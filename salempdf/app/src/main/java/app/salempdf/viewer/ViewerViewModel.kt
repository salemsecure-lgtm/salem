package app.salempdf.viewer

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.salempdf.domain.annotation.PdfAnnotation
import app.salempdf.domain.model.PageSize
import app.salempdf.domain.pageops.PageOp
import app.salempdf.domain.render.PageText
import app.salempdf.domain.render.PdfRenderSource
import app.salempdf.pdf.DocumentSession
import app.salempdf.pdf.PdfiumRenderSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

sealed interface ViewerUiState {
    data object Loading : ViewerUiState

    data class Ready(val pageCount: Int, val title: String) : ViewerUiState

    data class Failed(val message: String) : ViewerUiState
}

data class Selection(val pageIndex: Int, val range: IntRange)

class ViewerViewModel(
    application: Application,
    private val uri: Uri,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Loading)
    val uiState: StateFlow<ViewerUiState> = _uiState

    /** Page sizes in points; null until lazily loaded. Snapshot-backed so layout recomputes. */
    val pageSizes: SnapshotStateList<PageSize?> = mutableStateListOf()

    /** Bitmap provisioning (base pages, tiles, thumbnails, stamps) — see [RenderRequests]. */
    val renderer = RenderRequests(viewModelScope, { source }, pageSizes)

    private val _selection = MutableStateFlow<Selection?>(null)
    val selection: StateFlow<Selection?> = _selection

    // ---- Annotation editing state (Phase 2) ----
    val annotations: SnapshotStateList<PdfAnnotation> = mutableStateListOf()
    var activeTool by mutableStateOf<AnnotationTool?>(null)
        private set
    var toolColor by mutableStateOf(AnnotationTool.AMBER)
    var toolStrokeWidthPt by mutableStateOf(AnnotationTool.STROKE_WIDTHS_PT[1])
    var selectedAnnotationId by mutableStateOf<String?>(null)
        private set
    var isDirty by mutableStateOf(false)
        private set

    /** In-session reusable signature (PNG with transparent background). */
    var signaturePng: ByteArray? = null

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage

    private var session: DocumentSession? = null
    private var source: PdfRenderSource? = null
    private val pageTextCache = HashMap<Int, PageText>()

    init {
        viewModelScope.launch { openDocument() }
    }

    private suspend fun openDocument() {
        try {
            val opened = DocumentSession.open(getApplication(), uri)
            session = opened
            annotations.addAll(opened.initialAnnotations)
            val renderSource = PdfiumRenderSource.open(opened.renderFile)
            source = renderSource
            pageSizes.addAll(List(renderSource.pageCount) { null })
            _uiState.value = ViewerUiState.Ready(renderSource.pageCount, queryDisplayName())
            prefetchPageSizes(renderSource)
        } catch (e: IOException) {
            _uiState.value = ViewerUiState.Failed(e.message ?: "Couldn't open this PDF")
        }
    }

    /** Page 0 first (first-frame layout), then the rest so far pages settle in the background. */
    private fun prefetchPageSizes(src: PdfRenderSource) {
        viewModelScope.launch {
            for (index in 0 until src.pageCount) {
                if (pageSizes.getOrNull(index) == null) {
                    runCatching { pageSizes[index] = src.pageSize(index) }
                }
            }
        }
    }

    // ---- Annotation commands ----

    fun setTool(tool: AnnotationTool?) {
        activeTool = if (activeTool == tool) null else tool
        activeTool?.let { toolColor = it.defaultColor }
        selectedAnnotationId = null
        _selection.value = null
    }

    fun newAnnotationId(): String = "new:" + UUID.randomUUID().toString()

    fun addAnnotation(annotation: PdfAnnotation) {
        annotations.add(annotation)
        isDirty = true
    }

    fun replaceAnnotation(updated: PdfAnnotation) {
        val index = annotations.indexOfFirst { it.id == updated.id }
        if (index >= 0) {
            annotations[index] = updated
            isDirty = true
        }
    }

    fun selectAnnotation(id: String?) {
        selectedAnnotationId = id
    }

    fun deleteSelectedAnnotation() {
        val id = selectedAnnotationId ?: return
        annotations.removeAll { it.id == id }
        selectedAnnotationId = null
        isDirty = true
    }

    fun selectedAnnotation(): PdfAnnotation? = annotations.firstOrNull { it.id == selectedAnnotationId }

    fun applyColorToSelection(color: Int) {
        val selected = selectedAnnotation() ?: return
        val recolored: PdfAnnotation =
            when (selected) {
                is PdfAnnotation.TextMarkup -> selected.copy(colorRgb = color)
                is PdfAnnotation.Ink -> selected.copy(colorRgb = color)
                is PdfAnnotation.Shape -> selected.copy(colorRgb = color)
                is PdfAnnotation.Note -> selected.copy(colorRgb = color)
                is PdfAnnotation.FreeText -> selected.copy(colorRgb = color)
                is PdfAnnotation.Stamp -> selected
            }
        replaceAnnotation(recolored)
    }

    fun save() {
        val currentSession = session ?: return
        viewModelScope.launch {
            _saveMessage.value =
                when (val result = currentSession.save(annotations.toList())) {
                    is DocumentSession.SaveResult.Saved -> {
                        isDirty = false
                        "Changes saved"
                    }
                    is DocumentSession.SaveResult.SavedCopyOnly -> {
                        isDirty = false
                        "Saved to the app copy — the source didn't allow writing back"
                    }
                    is DocumentSession.SaveResult.Failed -> "Couldn't save: ${result.message}"
                }
        }
    }

    fun exportTo(target: Uri) {
        val currentSession = session ?: return
        viewModelScope.launch {
            _saveMessage.value =
                if (currentSession.exportTo(target)) "Copy saved" else "Couldn't save a copy"
        }
    }

    // ---- Page operations (Phase 3) ----

    var isWorking by mutableStateOf(false)
        private set

    fun applyPageOps(ops: List<PageOp>) {
        runStructuralOp { currentSession ->
            when (val result = currentSession.applyPageOps(annotations.toList(), ops)) {
                is DocumentSession.SaveResult.Saved -> "Pages updated"
                is DocumentSession.SaveResult.SavedCopyOnly -> "Pages updated in the app copy"
                is DocumentSession.SaveResult.Failed -> "Couldn't update pages: ${result.message}"
            }
        }
    }

    fun mergeWith(other: Uri) {
        runStructuralOp { currentSession ->
            when (val result = currentSession.mergeAppend(annotations.toList(), other)) {
                is DocumentSession.SaveResult.Saved -> "PDF merged"
                is DocumentSession.SaveResult.SavedCopyOnly -> "PDF merged into the app copy"
                is DocumentSession.SaveResult.Failed -> "Couldn't merge: ${result.message}"
            }
        }
    }

    fun extractTo(
        target: Uri,
        pages: List<Int>,
    ) {
        val currentSession = session ?: return
        viewModelScope.launch {
            isWorking = true
            val ok = currentSession.extractTo(target, annotations.toList(), pages)
            if (ok) isDirty = false
            isWorking = false
            _saveMessage.value =
                if (ok) "Extracted ${pages.size} page(s)" else "Couldn't extract pages"
        }
    }

    fun compressTo(target: Uri) {
        val currentSession = session ?: return
        viewModelScope.launch {
            isWorking = true
            val result = currentSession.compressTo(target, annotations.toList())
            if (result != null) isDirty = false
            isWorking = false
            _saveMessage.value =
                if (result == null) {
                    "Couldn't compress"
                } else {
                    val savedPercent = (result.savedFraction * 100).toInt().coerceAtLeast(0)
                    "Compressed copy saved — $savedPercent% smaller (${result.imagesRecompressed} image(s))"
                }
        }
    }

    /** Runs a working-file rewrite, then reloads the whole render/annotation state. */
    private fun runStructuralOp(op: suspend (DocumentSession) -> String) {
        val currentSession = session ?: return
        viewModelScope.launch {
            isWorking = true
            val message = op(currentSession)
            reloadFromSession(currentSession)
            isWorking = false
            _saveMessage.value = message
        }
    }

    private suspend fun reloadFromSession(currentSession: DocumentSession) {
        runCatching { source?.close() }
        source = null
        renderer.clearAll()
        pageTextCache.clear()
        _selection.value = null
        selectedAnnotationId = null
        annotations.clear()
        pageSizes.clear()
        isDirty = false
        try {
            annotations.addAll(currentSession.currentAnnotations())
            val renderSource = PdfiumRenderSource.open(currentSession.renderFile)
            source = renderSource
            pageSizes.addAll(List(renderSource.pageCount) { null })
            val title = (uiState.value as? ViewerUiState.Ready)?.title ?: queryDisplayName()
            _uiState.value = ViewerUiState.Ready(renderSource.pageCount, title)
            prefetchPageSizes(renderSource)
        } catch (e: IOException) {
            _uiState.value = ViewerUiState.Failed(e.message ?: "Couldn't reopen this PDF")
        }
    }

    fun consumeSaveMessage() {
        _saveMessage.value = null
    }

    suspend fun pageText(pageIndex: Int): PageText {
        val src = source ?: return PageText.EMPTY
        pageTextCache[pageIndex]?.let { return it }
        val text = runCatching { src.pageText(pageIndex) }.getOrDefault(PageText.EMPTY)
        if (pageTextCache.size >= PAGE_TEXT_CACHE_LIMIT) {
            pageTextCache.keys.firstOrNull()?.let(pageTextCache::remove)
        }
        pageTextCache[pageIndex] = text
        return text
    }

    fun setSelection(selection: Selection?) {
        _selection.value = selection
    }

    suspend fun selectedText(): String {
        val sel = _selection.value ?: return ""
        return pageText(sel.pageIndex).textIn(sel.range)
    }

    private suspend fun queryDisplayName(): String =
        withContext(Dispatchers.IO) {
            runCatching {
                getApplication<Application>()
                    .contentResolver
                    .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
            }.getOrNull() ?: uri.lastPathSegment ?: "Document"
        }

    override fun onCleared() {
        renderer.clearAll()
        runCatching { source?.close() }
        source = null
        session = null
    }

    companion object {
        private const val PAGE_TEXT_CACHE_LIMIT = 8

        fun factory(
            application: Application,
            uri: Uri,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = ViewerViewModel(application, uri) as T
            }
    }
}
