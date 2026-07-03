package app.salempdf.domain.viewer

/**
 * Quantizes continuous zoom into a small set of raster scales so bitmap cache
 * keys are reusable while pinching. The bucket is always >= the live zoom, so
 * drawn tiles are never upscaled beyond 1:1.
 */
object ZoomBuckets {
    const val MIN_ZOOM = 1f
    const val MAX_ZOOM = 8f

    private const val STEP = 1.5f

    fun bucketFor(zoom: Float): Float {
        var bucket = MIN_ZOOM
        while (bucket < zoom && bucket < MAX_ZOOM) {
            bucket = (bucket * STEP).coerceAtMost(MAX_ZOOM)
        }
        return bucket
    }
}
