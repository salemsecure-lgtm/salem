package app.salempdf.viewer

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateMapOf

/**
 * Byte-bounded LRU of rendered bitmaps backed by a snapshot state map, so
 * Compose draw code that reads [get] automatically redraws when a render
 * lands. Eviction drops the map entry only — bitmaps are reclaimed by GC,
 * never recycle()d, because a draw pass may still hold a frame's reference.
 */
class BitmapCache(private val maxBytes: Long) {
    private val bitmaps = mutableStateMapOf<String, Bitmap>()
    private val lruOrder = LinkedHashMap<String, Long>(16, 0.75f, true)
    private var totalBytes = 0L

    @Synchronized
    fun get(key: String): Bitmap? {
        val hit = bitmaps[key]
        if (hit != null) lruOrder[key] // touch access order
        return hit
    }

    @Synchronized
    fun contains(key: String): Boolean = bitmaps.containsKey(key)

    @Synchronized
    fun put(
        key: String,
        bitmap: Bitmap,
    ) {
        val size = bitmap.allocationByteCount.toLong()
        remove(key)
        bitmaps[key] = bitmap
        lruOrder[key] = size
        totalBytes += size
        evictIfNeeded()
    }

    @Synchronized
    fun clear() {
        bitmaps.clear()
        lruOrder.clear()
        totalBytes = 0
    }

    private fun remove(key: String) {
        val size = lruOrder.remove(key) ?: return
        bitmaps.remove(key)
        totalBytes -= size
    }

    private fun evictIfNeeded() {
        val iterator = lruOrder.entries.iterator()
        while (totalBytes > maxBytes && iterator.hasNext()) {
            val eldest = iterator.next()
            iterator.remove()
            bitmaps.remove(eldest.key)
            totalBytes -= eldest.value
        }
    }
}
