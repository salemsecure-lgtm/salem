package app.salempdf.viewer

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.salempdf.domain.model.PageSize
import app.salempdf.domain.render.PageText
import app.salempdf.domain.render.PdfRenderSource
import app.salempdf.domain.render.RenderRegion
import app.salempdf.domain.viewer.TileSpec
import app.salempdf.pdf.PdfiumRenderSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

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

    val baseCache = BitmapCache(BASE_CACHE_BYTES)
    val tileCache = BitmapCache(TILE_CACHE_BYTES)
    val thumbCache = BitmapCache(THUMB_CACHE_BYTES)

    private val _selection = MutableStateFlow<Selection?>(null)
    val selection: StateFlow<Selection?> = _selection

    private var source: PdfRenderSource? = null
    private val inFlight = mutableSetOf<String>()
    private val pageTextCache = HashMap<Int, PageText>()

    init {
        viewModelScope.launch { openDocument() }
    }

    private suspend fun openDocument() {
        try {
            val opened = PdfiumRenderSource.open(getApplication(), uri)
            source = opened
            pageSizes.addAll(List(opened.pageCount) { null })
            _uiState.value = ViewerUiState.Ready(opened.pageCount, queryDisplayName())
            prefetchPageSizes(opened)
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

    fun requestBase(
        pageIndex: Int,
        widthPx: Int,
    ) {
        val src = source ?: return
        val size = pageSizes.getOrNull(pageIndex) ?: return
        val heightPx = (widthPx / size.aspectRatio).toInt().coerceAtLeast(1)
        val key = "base:$pageIndex:$widthPx"
        render(baseCache, key, src, RenderRegion(pageIndex, widthPx, heightPx))
    }

    fun requestTile(spec: TileSpec) {
        val src = source ?: return
        if (pageSizes.getOrNull(spec.pageIndex) == null) return
        render(
            cache = tileCache,
            key = spec.cacheKey,
            src = src,
            region =
                RenderRegion(
                    pageIndex = spec.pageIndex,
                    pageWidthPx = spec.pageWidthPx,
                    pageHeightPx = spec.pageHeightPx,
                    left = spec.leftPx,
                    top = spec.topPx,
                    width = spec.widthPx,
                    height = spec.heightPx,
                ),
        )
    }

    fun requestThumbnail(pageIndex: Int) {
        val src = source ?: return
        val size = pageSizes.getOrNull(pageIndex) ?: return
        val heightPx = (THUMB_WIDTH_PX / size.aspectRatio).toInt().coerceAtLeast(1)
        render(thumbCache, thumbKey(pageIndex), src, RenderRegion(pageIndex, THUMB_WIDTH_PX, heightPx))
    }

    fun thumbKey(pageIndex: Int): String = "thumb:$pageIndex"

    private fun render(
        cache: BitmapCache,
        key: String,
        src: PdfRenderSource,
        region: RenderRegion,
    ) {
        if (cache.contains(key) || !inFlight.add(key)) return
        viewModelScope.launch {
            try {
                val bitmap: Bitmap = src.renderRegion(region)
                cache.put(key, bitmap)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (
                // Pdfium surfaces corrupt-page failures as assorted runtime types;
                // a failed tile just stays blank and the next scroll retries it.
                @Suppress("TooGenericExceptionCaught") ignored: Exception,
            ) {
                // Intentionally ignored.
            } finally {
                inFlight.remove(key)
            }
        }
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
                getApplication<Application>().contentResolver
                    .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
            }.getOrNull() ?: uri.lastPathSegment ?: "Document"
        }

    override fun onCleared() {
        baseCache.clear()
        tileCache.clear()
        thumbCache.clear()
        runCatching { source?.close() }
        source = null
    }

    companion object {
        private const val BASE_CACHE_BYTES = 48L * 1024 * 1024
        private const val TILE_CACHE_BYTES = 64L * 1024 * 1024
        private const val THUMB_CACHE_BYTES = 10L * 1024 * 1024
        private const val THUMB_WIDTH_PX = 144
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
