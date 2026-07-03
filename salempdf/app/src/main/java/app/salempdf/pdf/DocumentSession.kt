package app.salempdf.pdf

import android.content.Context
import android.net.Uri
import app.salempdf.domain.annotation.AnnotationPdfWriter
import app.salempdf.domain.annotation.PdfAnnotation
import app.salempdf.domain.annotation.PdfValidity
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
 * - Save: PdfBox writes to a temp file → structural validity check → Pdfium
 *   spot-render of page 1 → atomic swap into workingFile → write-back to the
 *   source uri. A failure at any step leaves the user's document untouched.
 */
class DocumentSession private constructor(
    private val context: Context,
    private val uri: Uri,
    val workingFile: File,
    val renderFile: File,
    val initialAnnotations: List<PdfAnnotation>,
) {
    private val writer = AnnotationPdfWriter()
    private val saveMutex = Mutex()

    sealed interface SaveResult {
        /** Saved and written back to the original document. */
        data object Saved : SaveResult

        /** Saved to the app's copy; the source uri refused the write-back. */
        data object SavedCopyOnly : SaveResult

        data class Failed(val message: String) : SaveResult
    }

    suspend fun save(annotations: List<PdfAnnotation>): SaveResult =
        saveMutex.withLock {
            withContext(Dispatchers.IO) {
                val temp = File(workingFile.parentFile, workingFile.name + ".tmp")
                try {
                    val expectedPages = PdfValidity.check(workingFile).getOrThrow()
                    writer.save(workingFile, temp, annotations)
                    PdfValidity.check(temp, expectedPageCount = expectedPages).getOrThrow()
                    spotRender(temp)
                    if (!temp.renameTo(workingFile)) {
                        temp.copyTo(workingFile, overwrite = true)
                        temp.delete()
                    }
                    if (writeBackToSource()) SaveResult.Saved else SaveResult.SavedCopyOnly
                } catch (e: IOException) {
                    temp.delete()
                    SaveResult.Failed(e.message ?: "Couldn't save changes")
                }
            }
        }

    /** Copies the saved working file to [target] (the "Save a copy" flow). */
    suspend fun exportTo(target: Uri): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                context.contentResolver.openOutputStream(target, "wt")?.use { out ->
                    workingFile.inputStream().use { it.copyTo(out) }
                } ?: throw IOException("Could not open destination")
                true
            }.getOrDefault(false)
        }

    fun cleanUp() {
        workingFile.delete()
        renderFile.delete()
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
