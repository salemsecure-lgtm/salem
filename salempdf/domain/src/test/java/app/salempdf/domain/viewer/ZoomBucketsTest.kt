package app.salempdf.domain.viewer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ZoomBucketsTest {
    @Test
    fun `bucket is never below the live zoom`() {
        var z = 1f
        while (z <= ZoomBuckets.MAX_ZOOM) {
            assertTrue(ZoomBuckets.bucketFor(z) >= z)
            z += 0.1f
        }
    }

    @Test
    fun `zoom one maps to bucket one`() {
        assertEquals(1f, ZoomBuckets.bucketFor(1f), 0f)
    }

    @Test
    fun `buckets are capped at max zoom`() {
        assertEquals(ZoomBuckets.MAX_ZOOM, ZoomBuckets.bucketFor(100f), 0f)
    }

    @Test
    fun `nearby zooms share a bucket`() {
        assertEquals(ZoomBuckets.bucketFor(1.6f), ZoomBuckets.bucketFor(2.1f), 0f)
    }
}
