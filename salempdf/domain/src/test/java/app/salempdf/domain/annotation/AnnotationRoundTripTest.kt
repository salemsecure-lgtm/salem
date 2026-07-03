package app.salempdf.domain.annotation

import android.graphics.Bitmap
import android.graphics.Color
import app.salempdf.domain.render.RectPt
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Round-trip tests for every annotation write path (SPEC convention: write ->
 * reopen -> assert valid & intact). Outputs are additionally parsed with
 * pure-JVM Apache PDFBox — an independent codebase standing in for the
 * "renders in a second viewer" gate criterion in CI.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AnnotationRoundTripTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val writer = AnnotationPdfWriter()
    private lateinit var source: File
    private lateinit var target: File

    private val amber = 0xFBBF24
    private val indigo = 0x4F46E5

    @Before
    fun setUp() {
        PDFBoxResourceLoader.init(RuntimeEnvironment.getApplication())
        source = folder.newFile("source.pdf")
        target = folder.newFile("target.pdf")
        PDDocument().use { doc ->
            repeat(3) { doc.addPage(PDPage(PDRectangle.LETTER)) }
            doc.save(source)
        }
    }

    private fun roundTrip(vararg annotations: PdfAnnotation): List<PdfAnnotation> =
        runBlocking {
            writer.save(source, target, annotations.toList())
            assertTrue(PdfValidity.check(target, expectedPageCount = 3).isSuccess)
            writer.readSalemAnnotations(target)
        }

    /** Parses [target] with the independent Apache PDFBox and returns page [pageIndex]'s annotations. */
    private fun <T> withApacheAnnotations(
        pageIndex: Int,
        block: (List<org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation>) -> T,
    ): T =
        org.apache.pdfbox.pdmodel.PDDocument.load(target).use { doc ->
            block(doc.getPage(pageIndex).annotations)
        }

    private fun assertHasAppearance(annotation: org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation) {
        val stream = annotation.normalAppearanceStream
        assertNotNull("annotation must carry a normal appearance stream (/AP /N)", stream)
        val bytes = stream.cosObject.createInputStream().use { it.readBytes() }
        assertTrue("appearance stream must have content", bytes.isNotEmpty())
    }

    @Test
    fun `highlight round-trips and is standard`() {
        val rects = listOf(RectPt(72f, 100f, 300f, 114f), RectPt(72f, 118f, 200f, 132f))
        val result =
            roundTrip(
                PdfAnnotation.TextMarkup("new:h1", 0, amber, 0.4f, MarkupKind.HIGHLIGHT, rects),
            )
        assertEquals(1, result.size)
        val highlight = result.single() as PdfAnnotation.TextMarkup
        assertEquals(MarkupKind.HIGHLIGHT, highlight.kind)
        assertEquals(2, highlight.lineRects.size)
        assertEquals(72f, highlight.lineRects[0].left, 0.5f)
        assertEquals(100f, highlight.lineRects[0].top, 0.5f)
        assertEquals(0.4f, highlight.opacity, 0.01f)
        assertEquals(amber, highlight.colorRgb)

        withApacheAnnotations(0) { annotations ->
            val markup = annotations.single()
            assertEquals("Highlight", markup.subtype)
            assertTrue(markup.annotationName.startsWith(SALEM_ANNOTATION_PREFIX))
            assertHasAppearance(markup)
        }
    }

    @Test
    fun `underline and strikeout round-trip`() {
        val rect = listOf(RectPt(72f, 200f, 300f, 214f))
        val result =
            roundTrip(
                PdfAnnotation.TextMarkup("new:u1", 1, indigo, 1f, MarkupKind.UNDERLINE, rect),
                PdfAnnotation.TextMarkup("new:s1", 1, 0xFF0000, 1f, MarkupKind.STRIKEOUT, rect),
            )
        assertEquals(
            setOf(MarkupKind.UNDERLINE, MarkupKind.STRIKEOUT),
            result.filterIsInstance<PdfAnnotation.TextMarkup>().map { it.kind }.toSet(),
        )
        withApacheAnnotations(1) { annotations ->
            assertEquals(setOf("Underline", "StrikeOut"), annotations.map { it.subtype }.toSet())
            annotations.forEach(::assertHasAppearance)
        }
    }

    @Test
    fun `ink strokes round-trip`() {
        val strokes =
            listOf(
                listOf(PointPt(100f, 100f), PointPt(150f, 120f), PointPt(200f, 100f)),
                listOf(PointPt(100f, 160f), PointPt(200f, 160f)),
            )
        val result = roundTrip(PdfAnnotation.Ink("new:i1", 0, indigo, 1f, strokes, strokeWidthPt = 3f))
        val ink = result.single() as PdfAnnotation.Ink
        assertEquals(2, ink.strokes.size)
        assertEquals(3, ink.strokes[0].size)
        assertEquals(150f, ink.strokes[0][1].x, 0.5f)
        assertEquals(120f, ink.strokes[0][1].y, 0.5f)
        assertEquals(3f, ink.strokeWidthPt, 0.01f)

        withApacheAnnotations(0) { annotations ->
            val apacheInk = annotations.single()
            assertEquals("Ink", apacheInk.subtype)
            assertHasAppearance(apacheInk)
        }
    }

    @Test
    fun `rect oval line and arrow round-trip`() {
        val result =
            roundTrip(
                PdfAnnotation.Shape("new:r", 2, indigo, 1f, ShapeKind.RECT, PointPt(50f, 50f), PointPt(150f, 100f), 2f),
                PdfAnnotation.Shape(
                    "new:o",
                    2,
                    indigo,
                    1f,
                    ShapeKind.OVAL,
                    PointPt(200f, 50f),
                    PointPt(300f, 100f),
                    2f,
                ),
                PdfAnnotation.Shape(
                    "new:l",
                    2,
                    indigo,
                    1f,
                    ShapeKind.LINE,
                    PointPt(50f, 200f),
                    PointPt(150f, 250f),
                    2f,
                ),
                PdfAnnotation.Shape(
                    "new:a",
                    2,
                    indigo,
                    1f,
                    ShapeKind.ARROW,
                    PointPt(200f, 200f),
                    PointPt(300f, 250f),
                    2f,
                ),
            )
        val byKind = result.filterIsInstance<PdfAnnotation.Shape>().associateBy { it.kind }
        assertEquals(4, byKind.size)
        val rect = byKind.getValue(ShapeKind.RECT)
        assertEquals(50f, rect.start.x, 0.6f)
        assertEquals(50f, rect.start.y, 0.6f)
        assertEquals(150f, rect.end.x, 0.6f)
        val arrow = byKind.getValue(ShapeKind.ARROW)
        assertEquals(200f, arrow.start.x, 0.5f)
        assertEquals(250f, arrow.end.y, 0.5f)

        withApacheAnnotations(2) { annotations ->
            assertEquals(4, annotations.size)
            assertEquals(setOf("Square", "Circle", "Line"), annotations.map { it.subtype }.toSet())
            annotations.forEach(::assertHasAppearance)
        }
    }

    @Test
    fun `note contents round-trip`() {
        val result =
            roundTrip(PdfAnnotation.Note("new:n1", 0, amber, 1f, PointPt(400f, 80f), "Check this paragraph"))
        val note = result.single() as PdfAnnotation.Note
        assertEquals("Check this paragraph", note.contents)
        assertEquals(400f, note.at.x, 0.5f)
        assertEquals(80f, note.at.y, 0.5f)

        withApacheAnnotations(0) { annotations ->
            val text = annotations.single()
            assertEquals("Text", text.subtype)
            assertEquals("Check this paragraph", text.contents)
        }
    }

    @Test
    fun `freetext round-trips text and font size`() {
        val result =
            roundTrip(
                PdfAnnotation.FreeText(
                    id = "new:f1",
                    pageIndex = 1,
                    colorRgb = 0x1F2937,
                    opacity = 1f,
                    rect = RectPt(72f, 300f, 400f, 360f),
                    text = "Salem PDF freetext",
                    fontSizePt = 14f,
                ),
            )
        val freeText = result.single() as PdfAnnotation.FreeText
        assertEquals("Salem PDF freetext", freeText.text)
        assertEquals(14f, freeText.fontSizePt, 0.01f)
        assertEquals(72f, freeText.rect.left, 0.5f)

        withApacheAnnotations(1) { annotations ->
            val apacheFreeText = annotations.single()
            assertEquals("FreeText", apacheFreeText.subtype)
            assertEquals("Salem PDF freetext", apacheFreeText.contents)
            assertHasAppearance(apacheFreeText)
        }
    }

    @Test
    fun `image stamp round-trips with embedded appearance`() {
        val png = redSquarePng()
        val result =
            roundTrip(
                PdfAnnotation.Stamp("new:st1", 0, 0, 1f, RectPt(100f, 500f, 220f, 560f), png),
            )
        val stamp = result.single() as PdfAnnotation.Stamp
        assertEquals(100f, stamp.rect.left, 0.5f)
        assertEquals(560f, stamp.rect.bottom, 0.5f)
        assertTrue("re-extracted image must not be empty", stamp.pngBytes.isNotEmpty())

        withApacheAnnotations(0) { annotations ->
            val apacheStamp = annotations.single()
            assertEquals("Stamp", apacheStamp.subtype)
            assertHasAppearance(apacheStamp)
            val resources = apacheStamp.normalAppearanceStream.resources
            assertTrue("appearance must embed an image XObject", resources.xObjectNames.iterator().hasNext())
        }
    }

    @Test
    fun `foreign annotations are preserved and ours are replaceable`() {
        // A "foreign" annotation: written directly (no Salem /NM name).
        PDDocument.load(source).use { doc ->
            val page = doc.getPage(0)
            val foreign =
                com.tom_roush.pdfbox.pdmodel.interactive.annotation
                    .PDAnnotationText()
            foreign.contents = "someone else's note"
            foreign.rectangle = PDRectangle(10f, 10f, 20f, 20f)
            page.annotations.add(foreign)
            doc.save(source)
        }

        runBlocking {
            writer.save(source, target, listOf(PdfAnnotation.Note("new:mine", 0, amber, 1f, PointPt(50f, 50f), "mine")))
            // Re-save from target with an empty Salem set: ours disappears, foreign survives.
            val emptied = folder.newFile("emptied.pdf")
            writer.save(target, emptied, emptyList())
            org.apache.pdfbox.pdmodel.PDDocument.load(emptied).use { doc ->
                val remaining = doc.getPage(0).annotations
                assertEquals(1, remaining.size)
                assertEquals("someone else's note", remaining.single().contents)
            }
        }
    }

    @Test
    fun `moving an annotation rewrites it in place`() {
        val original = PdfAnnotation.Note("new:m1", 0, amber, 1f, PointPt(100f, 100f), "movable")
        runBlocking {
            writer.save(source, target, listOf(original))
            val parsed = writer.readSalemAnnotations(target).single()
            val moved = parsed.movedBy(dx = 50f, dy = -20f)
            val movedFile = folder.newFile("moved.pdf")
            writer.save(target, movedFile, listOf(moved))
            val reparsed = writer.readSalemAnnotations(movedFile).single() as PdfAnnotation.Note
            assertEquals(150f, reparsed.at.x, 0.5f)
            assertEquals(80f, reparsed.at.y, 0.5f)
            org.apache.pdfbox.pdmodel.PDDocument.load(movedFile).use { doc ->
                assertEquals(1, doc.getPage(0).annotations.size)
            }
        }
    }

    @Test
    fun `strip removes only salem annotations`() {
        runBlocking {
            writer.save(source, target, listOf(PdfAnnotation.Note("new:x", 0, amber, 1f, PointPt(9f, 9f), "temp")))
            val stripped = folder.newFile("stripped.pdf")
            writer.stripSalemAnnotations(target, stripped)
            assertEquals(0, writer.readSalemAnnotations(stripped).size)
            assertTrue(PdfValidity.check(stripped, expectedPageCount = 3).isSuccess)
        }
    }

    private fun redSquarePng(): ByteArray {
        val bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.RED)
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        return out.toByteArray()
    }
}
