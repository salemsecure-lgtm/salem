package app.salempdf.domain.pageops

import android.graphics.Bitmap
import android.graphics.Color
import app.salempdf.domain.annotation.AnnotationPdfWriter
import app.salempdf.domain.annotation.PdfAnnotation
import app.salempdf.domain.annotation.PdfValidity
import app.salempdf.domain.annotation.PointPt
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.File

/**
 * Round-trip tests for every page operation (SPEC: every output passes a PDF
 * validity check and reopens cleanly). Page identity is tracked through ops
 * via a Salem note annotation per page — which also proves annotations travel
 * with their pages. Outputs are re-parsed with pure-JVM Apache PDFBox as the
 * independent second parser.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PageOpsRoundTripTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val ops = PageOpsWriter()
    private val annotations = AnnotationPdfWriter()
    private lateinit var source: File
    private lateinit var target: File

    @Before
    fun setUp() {
        PDFBoxResourceLoader.init(RuntimeEnvironment.getApplication())
        source = folder.newFile("source.pdf")
        target = folder.newFile("target.pdf")
        val blank = folder.newFile("blank.pdf")
        PDDocument().use { doc ->
            repeat(5) { doc.addPage(PDPage(PDRectangle.LETTER)) }
            doc.save(blank)
        }
        // One identifying note per page.
        runBlocking {
            annotations.save(
                blank,
                source,
                (0 until 5).map { page ->
                    PdfAnnotation.Note("new:p$page", page, 0xFBBF24, 1f, PointPt(40f, 40f), "page $page")
                },
            )
        }
    }

    /** Note contents per page, in page order — the identity fingerprint. */
    private fun fingerprint(file: File): List<String> =
        runBlocking {
            annotations
                .readSalemAnnotations(file)
                .filterIsInstance<PdfAnnotation.Note>()
                .sortedBy { it.pageIndex }
                .map { it.contents }
        }

    private fun assertValid(
        file: File,
        expectedPages: Int,
    ) {
        runBlocking { assertTrue(PdfValidity.check(file, expectedPages).isSuccess) }
        // Independent parser: reopen with Apache PDFBox.
        org.apache.pdfbox.pdmodel.PDDocument.load(file).use { doc ->
            assertEquals(expectedPages, doc.numberOfPages)
        }
    }

    @Test
    fun `rotate persists in page rotation and survives reopen`() {
        runBlocking { ops.applyOps(source, target, listOf(PageOp.Rotate(pages = listOf(1, 3), clockwiseTurns = 1))) }
        assertValid(target, 5)
        org.apache.pdfbox.pdmodel.PDDocument.load(target).use { doc ->
            assertEquals(90, doc.getPage(1).rotation)
            assertEquals(90, doc.getPage(3).rotation)
            assertEquals(0, doc.getPage(0).rotation)
        }
        // Four turns come back to zero.
        val fullCircle = folder.newFile("full.pdf")
        runBlocking { ops.applyOps(target, fullCircle, listOf(PageOp.Rotate(listOf(1), clockwiseTurns = 3))) }
        org.apache.pdfbox.pdmodel.PDDocument.load(fullCircle).use { doc ->
            assertEquals(0, doc.getPage(1).rotation)
        }
    }

    @Test
    fun `delete removes pages and their annotations travel away`() {
        runBlocking { ops.applyOps(source, target, listOf(PageOp.Delete(listOf(1, 3)))) }
        assertValid(target, 3)
        assertEquals(listOf("page 0", "page 2", "page 4"), fingerprint(target))
    }

    @Test
    fun `reorder permutes pages with annotations attached`() {
        runBlocking { ops.applyOps(source, target, listOf(PageOp.Reorder(listOf(4, 3, 2, 1, 0)))) }
        assertValid(target, 5)
        assertEquals(listOf("page 4", "page 3", "page 2", "page 1", "page 0"), fingerprint(target))
    }

    @Test
    fun `move shifts a single page`() {
        runBlocking { ops.applyOps(source, target, listOf(PageOp.Move(from = 0, to = 3))) }
        assertValid(target, 5)
        assertEquals(listOf("page 1", "page 2", "page 3", "page 0", "page 4"), fingerprint(target))
    }

    @Test
    fun `insert blank adds an empty page of the requested size`() {
        runBlocking {
            ops.applyOps(
                source,
                target,
                listOf(PageOp.InsertBlank(at = 2, widthPt = 300f, heightPt = 500f)),
            )
        }
        assertValid(target, 6)
        assertEquals(listOf("page 0", "page 1", "page 2", "page 3", "page 4"), fingerprint(target))
        org.apache.pdfbox.pdmodel.PDDocument.load(target).use { doc ->
            val inserted = doc.getPage(2)
            assertEquals(300f, inserted.mediaBox.width, 0.1f)
            assertEquals(500f, inserted.mediaBox.height, 0.1f)
            assertEquals(0, inserted.annotations.size)
        }
    }

    @Test
    fun `extract copies pages to a new document and leaves the source intact`() {
        runBlocking { ops.extractPages(source, target, listOf(1, 3)) }
        assertValid(target, 2)
        assertEquals(listOf("page 1", "page 3"), fingerprint(target))
        assertValid(source, 5)
        assertEquals(5, fingerprint(source).size)
    }

    @Test
    fun `merge concatenates documents in order`() {
        val second = folder.newFile("second.pdf")
        val blank = folder.newFile("blank2.pdf")
        PDDocument().use { doc ->
            repeat(2) { doc.addPage(PDPage(PDRectangle.A4)) }
            doc.save(blank)
        }
        runBlocking {
            annotations.save(
                blank,
                second,
                listOf(PdfAnnotation.Note("new:m0", 0, 0x4F46E5, 1f, PointPt(40f, 40f), "merged 0")),
            )
            ops.merge(listOf(source, second), target)
        }
        assertValid(target, 7)
        assertEquals(
            listOf("page 0", "page 1", "page 2", "page 3", "page 4", "merged 0"),
            fingerprint(target),
        )
    }

    @Test
    fun `compress shrinks image-heavy documents and stays valid`() {
        val imageDoc = folder.newFile("images.pdf")
        PDDocument().use { doc ->
            val page = PDPage(PDRectangle.LETTER)
            doc.addPage(page)
            val bitmap = noisyBitmap(2000, 2000)
            val image = JPEGFactory.createFromImage(doc, bitmap, 0.95f)
            PDPageContentStream(doc, page).use { content ->
                content.drawImage(image, 0f, 0f, 612f, 792f)
            }
            doc.save(imageDoc)
        }
        val result = runBlocking { ops.compress(imageDoc, target, quality = 0.5f, maxDimensionPx = 800) }
        assertValid(target, 1)
        assertEquals(1, result.imagesRecompressed)
        assertTrue(
            "expected smaller output, was ${result.bytesBefore} -> ${result.bytesAfter}",
            result.bytesAfter < result.bytesBefore,
        )
    }

    /**
     * Deterministic high-frequency pattern so JPEG has real entropy to shed.
     * RGB_565 (no alpha) — like real scanned-PDF JPEGs — so JPEGFactory does
     * not attach a soft mask, which compress conservatively refuses to touch.
     */
    private fun noisyBitmap(
        width: Int,
        height: Int,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val pixels = IntArray(width * height)
        var seed = 0x2F6E2B1
        for (i in pixels.indices) {
            seed = seed * 1103515245 + 12345
            pixels[i] = Color.rgb(seed ushr 16 and 0xFF, seed ushr 8 and 0xFF, seed and 0xFF)
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }
}
