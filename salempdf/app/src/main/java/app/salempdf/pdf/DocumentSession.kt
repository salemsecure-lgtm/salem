package app.salempdf.pdf

import android.content.Context
import android.net.Uri
import app.salempdf.domain.annotation.AnnotationPdfWriter
import app.salempdf.domain.annotation.PdfAnnotation
import app.salempdf.domain.annotation.PdfValidity
import app.salempdf.domain.pageops.CompressResult
import app.salempdf.domain.pageops.PageOp
import app.salempdf.domain.pageops.PageOpsWriter
import app.salempdf.domain.render.RenderRegion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * An editing session for one document (SPEC write-path safety):
 *
 * - [workingFile]: cache copy of the source — the authoritative bytes.
 * - [renderFile]: workingFile with Salem annotations stripped; Pdfium renders
 *   this, and the Compose overlay draws the (editable) Salem annotations on
 *   top, so nothing is ever drawn twice.
 * - Every rewrite (annotation save, page ops, merge): PdfBox writes to a temp
 *   file → structural validity check → Pdfium spot-render of page 1 → atomic
 *   swap into workingFile → write-back to the source uri. A failure at any
 *   step leaves the user's document untouched.
 */
class DocumentSession private constructor(
    private val context: Context,
    private val uri: Uri,
    val workingFile: File,
    val renderFile: File,
    val initialAnnotations: List<PdfAnnotation>,
) {
    private val writer = AnnotationPdfWriter()
    private val pageOps = PageOpsWriter()
    private val saveMutex = Mutex()

    sealed interface SaveResult {
        /** Saved and written back to the original document. */
        data object Saved : SaveResult

        /** Saved to the app's copy; the source uri refused the write-back. */
        data object SavedCopyOnly : SaveResult

        data class Failed(val message: String) : SaveResult
    }

    suspend fun save(annotations: List<PdfAnnotation>): SaveResult =
        rewriteWorking(annotations, expectSamePageCount = true, transform = null)

    /** Applies [ops] on top of the current annotations (which are saved first, in the same rewrite). */
    suspend fun applyPageOps(
        annotations: List<PdfAnnotation>,
        ops: List<PageOp>,
    ): SaveResult =
        rewriteWorking(annotations, expectSamePageCount = false) { from, to ->
            pageOps.applyOps(from, to, ops)
        }

    /** Appends every page of [other] to this document. */
    suspend fun mergeAppend(
        annotations: List<PdfAnnotation>,
        other: Uri,
    ): SaveResult {
        val extra = File(workingFile.parentFile, workingFile.name + ".merge")
        return try {
            copyUriToFile(other, extra)
            PdfValidity.check(extra).getOrElse {
                return SaveResult.Failed("That file isn't a readable PDF")
            }
            rewriteWorking(annotations, expectSamePageCount = false) { from, to ->
                pageOps.merge(listOf(from, extra), to)
            }
        } finally {
            extra.delete()
        }
    }

    /** Saves, then copies [pages] into a fresh document at [target]. */
    suspend fun extractTo(
        target: Uri,
        annotations: List<PdfAnnotation>,
        pages: List<Int>,
    ): Boolean {
        if (save(annotations) is SaveResult.Failed) return false
        return withContext(Dispatchers.IO) {
            runCatching {
                val temp = File(workingFile.parentFile, workingFile.name + ".extract")
                try {
                    pageOps.extractPages(workingFile, temp, pages)
                    PdfValidity.check(temp, expectedPageCount = pages.size).getOrThrow()
                    streamToUri(temp, target)
                    true
                } finally {
                    temp.delete()
                }
            }.getOrDefault(false)
        }
    }

    /** Saves, then writes an image-downsampled copy to [target]; null on failure. */
    suspend fun compressTo(
        target: Uri,
        annotations: List<PdfAnnotation>,
    ): CompressResult? {
        if (save(annotations) is SaveResult.Failed) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val temp = File(workingFile.parentFile, workingFile.name + ".compress")
                try {
                    val result = pageOps.compress(workingFile, temp)
                    PdfValidity.check(temp).getOrThrow()
                    streamToUri(temp, target)
                    result
                } finally {
                    temp.delete()
                }
            }.getOrNull()
        }
    }

    /** Copies the saved working file to [target] (the "Save a copy" flow). */
    suspend fun exportTo(target: Uri): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                streamToUri(workingFile, target)
                true
            }.getOrDefault(false)
        }

    /** Re-reads Salem annotations from the working file (after structural changes). */
    suspend fun currentAnnotations(): List<PdfAnnotation> =
        runCatching { writer.readSalemAnnotations(workingFile) }.getOrDefault(emptyList())

    fun cleanUp() {
        workingFile.delete()
        renderFile.delete()
    }

    /**
     * The one rewrite pipeline. Writes [annotations] into a temp copy, runs
     * the optional structural [transform], validates, spot-renders, swaps,
     * refreshes the render layer, and writes back to the source.
     */
    private suspend fun rewriteWorking(
        annotations: List<PdfAnnotation>,
        expectSamePageCount: Boolean,
        transform: (suspend (File, File) -> Unit)?,
    ): SaveResult =
        saveMutex.withLock {
            withContext(Dispatchers.IO) {
                val annotated = File(workingFile.parentFile, workingFile.name + ".tmp1")
                val transformed = File(workingFile.parentFile, workingFile.name + ".tmp2")
                try {
                    val pagesBefore = PdfValidity.check(workingFile).getOrThrow()
                    writer.save(workingFile, annotated, annotations)
                    val candidate =
                        if (transform != null) {
                            transform(annotated, transformed)
                            transformed
                        } else {
                            annotated
                        }
                    val expected = if (expectSamePageCount) pagesBefore else null
                    PdfValidity.check(candidate, expectedPageCount = expected).getOrThrow()
                    spotRender(candidate)
                    if (!candidate.renameTo(workingFile)) {
                        candidate.copyTo(workingFile, overwrite = true)
                    }
                    refreshRenderFile()
                    if (writeBackToSource()) SaveResult.Saved else SaveResult.SavedCopyOnly
                } catch (e: IOException) {
                    SaveResult.Failed(e.message ?: "Couldn't save changes")
                } catch (e: IllegalArgumentException) {
                    SaveResult.Failed(e.message ?: "Couldn't apply that operation")
                } finally {
                    annotated.delete()
                    transformed.delete()
                }
            }
        }

    private suspend fun refreshRenderFile() {
        runCatching { writer.stripSalemAnnotations(workingFile, renderFile) }
            .onFailure { workingFile.copyTo(renderFile, overwrite = true) }
    }

    /** Renders a small region of page 1 as the renderer-level validity check. */
    private suspend fun spotRender(file: File) {
        val source = PdfiumRenderSource.open(file)
        try {
            source.renderRegion(RenderRegion(pageIndex = 0, pageWidthPx = 64, pageHeightPx = 64))
        } finally {
            runCatching { source.close() }
        }
    }

    private fun streamToUri(
        from: File,
        target: Uri,
    ) {
        context.contentResolver.openOutputStream(target, "wt")?.use { out ->
            from.inputStream().use { it.copyTo(out) }
        } ?: throw IOException("Could not open destination")
    }

    private fun copyUriToFile(
        from: Uri,
        target: File,
    ) {
        context.contentResolver.openInputStream(from)?.use { input ->
            target.outputStream().use { input.copyTo(it) }
        } ?: throw IOException("Could not open document")
    }

    private fun writeBackToSource(): Boolean =
        runCatching {
            context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
                workingFile.inputStream().use { it.copyTo(out) }
            } ?: return false
            true
        }.getOrDefault(false)

    companion object {
        suspend fun open(
            context: Context,
            uri: Uri,
        ): DocumentSession =
            withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "sessions").apply { mkdirs() }
                val key = Integer.toHexString(uri.toString().hashCode())
                val working = File(dir, "doc-$key.pdf")
                val render = File(dir, "render-$key.pdf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    working.outputStream().use { input.copyTo(it) }
                } ?: throw IOException("Could not open document")

                val writer = AnnotationPdfWriter()
                val annotations =
                    runCatching { writer.readSalemAnnotations(working) }.getOrDefault(emptyList())
                if (annotations.isEmpty()) {
                    working.copyTo(render, overwrite = true)
                } else {
                    writer.stripSalemAnnotations(working, render)
                }
                DocumentSession(context.applicationContext, uri, working, render, annotations)
            }
    }
}
