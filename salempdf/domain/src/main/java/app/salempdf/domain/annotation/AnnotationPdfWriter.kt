package app.salempdf.domain.annotation

import android.graphics.Bitmap
import app.salempdf.domain.render.RectPt
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDColor
import com.tom_roush.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotation
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLine
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationSquareCircle
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationText
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

/** Identity prefix for annotations Salem PDF owns (stored in each dictionary's /NM). */
const val SALEM_ANNOTATION_PREFIX = "salempdf:"

/**
 * Write side of the annotation engine (PdfBox). Every annotation becomes a
 * standard PDF annotation dictionary with a generated appearance stream, so
 * any viewer renders it. Salem-owned annotations are identified by their /NM
 * prefix; foreign annotations are never touched.
 */
class AnnotationPdfWriter {
    /**
     * Writes [source] to [target] with Salem annotations replaced by
     * [annotations] (the full desired set — add/move/delete are all "replace").
     */
    suspend fun save(
        source: File,
        target: File,
        annotations: List<PdfAnnotation>,
    ): Unit =
        withContext(Dispatchers.IO) {
            PDDocument.load(source).use { doc ->
                removeSalemAnnotations(doc)
                for (annotation in annotations) {
                    appendAnnotation(doc, annotation)
                }
                doc.save(target)
            }
        }

    /** Writes a copy of [source] to [target] without Salem annotations (the render layer). */
    suspend fun stripSalemAnnotations(
        source: File,
        target: File,
    ): Unit =
        withContext(Dispatchers.IO) {
            PDDocument.load(source).use { doc ->
                removeSalemAnnotations(doc)
                doc.save(target)
            }
        }

    /** Parses Salem-owned annotations back into the domain model (the edit round-trip). */
    suspend fun readSalemAnnotations(source: File): List<PdfAnnotation> =
        withContext(Dispatchers.IO) {
            PDDocument.load(source).use { doc ->
                val result = mutableListOf<PdfAnnotation>()
                for (pageIndex in 0 until doc.numberOfPages) {
                    val page = doc.getPage(pageIndex)
                    val box = page.pageBox()
                    for (annotation in page.annotations) {
                        val name = annotation.annotationName ?: continue
                        if (!name.startsWith(SALEM_ANNOTATION_PREFIX)) continue
                        parseAnnotation(annotation, name, pageIndex, box)?.let(result::add)
                    }
                }
                result
            }
        }

    private fun removeSalemAnnotations(doc: PDDocument) {
        for (pageIndex in 0 until doc.numberOfPages) {
            val page = doc.getPage(pageIndex)
            page.annotations =
                page.annotations.filterNot { it.annotationName?.startsWith(SALEM_ANNOTATION_PREFIX) == true }
        }
    }

    private fun appendAnnotation(
        doc: PDDocument,
        annotation: PdfAnnotation,
    ) {
        val page = doc.getPage(annotation.pageIndex)
        val box = page.pageBox()
        val pdAnnotation =
            when (annotation) {
                is PdfAnnotation.TextMarkup -> buildTextMarkup(annotation, box)
                is PdfAnnotation.Ink -> buildInk(annotation, box)
                is PdfAnnotation.Shape -> buildShape(annotation, box)
                is PdfAnnotation.Note -> buildNote(annotation, box)
                is PdfAnnotation.FreeText -> buildFreeText(annotation, box)
                is PdfAnnotation.Stamp -> buildStamp(doc, annotation, box)
            }
        pdAnnotation.annotationName = sessionName(annotation.id)
        pdAnnotation.setPrinted(true)
        pdAnnotation.color = annotation.colorRgb.toPdColor()
        if (pdAnnotation is PDAnnotationMarkup && annotation.opacity < 1f) {
            pdAnnotation.constantOpacity = annotation.opacity
        }
        // Stamps carry a hand-built appearance; everything else uses the standard handlers.
        if (annotation !is PdfAnnotation.Stamp) {
            pdAnnotation.constructAppearances(doc)
        }
        page.annotations.add(pdAnnotation)
    }

    private fun buildTextMarkup(
        annotation: PdfAnnotation.TextMarkup,
        box: PageBox,
    ): PDAnnotationTextMarkup {
        val subtype =
            when (annotation.kind) {
                MarkupKind.HIGHLIGHT -> PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT
                MarkupKind.UNDERLINE -> PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE
                MarkupKind.STRIKEOUT -> PDAnnotationTextMarkup.SUB_TYPE_STRIKEOUT
            }
        val markup = PDAnnotationTextMarkup(subtype)
        // Quad order per PDF spec: upper-left, upper-right, lower-left, lower-right
        // (axis-aligned in PDF user space; the rotation-aware mapper normalizes).
        val quads = FloatArray(annotation.lineRects.size * 8)
        annotation.lineRects.forEachIndexed { i, rect ->
            val pdfRect = box.toPdfRect(rect)
            val left = pdfRect[0]
            val bottom = pdfRect[1]
            val right = pdfRect[2]
            val top = pdfRect[3]
            var q = i * 8
            quads[q++] = left
            quads[q++] = top
            quads[q++] = right
            quads[q++] = top
            quads[q++] = left
            quads[q++] = bottom
            quads[q++] = right
            quads[q] = bottom
        }
        markup.quadPoints = quads
        markup.rectangle = annotation.bounds.toPdRectangle(box)
        return markup
    }

    private fun buildInk(
        annotation: PdfAnnotation.Ink,
        box: PageBox,
    ): PDAnnotationMarkup {
        val ink = PDAnnotationMarkup()
        ink.cosObject.setName(COSName.SUBTYPE, PDAnnotationMarkup.SUB_TYPE_INK)
        ink.setInkList(
            annotation.strokes
                .map { stroke ->
                    FloatArray(stroke.size * 2).also { flat ->
                        stroke.forEachIndexed { i, point ->
                            val pdfPoint = box.toPdfPoint(point)
                            flat[i * 2] = pdfPoint.x
                            flat[i * 2 + 1] = pdfPoint.y
                        }
                    }
                }.toTypedArray(),
        )
        ink.borderStyle = borderStyle(annotation.strokeWidthPt)
        ink.rectangle = annotation.bounds.toPdRectangle(box)
        return ink
    }

    private fun buildShape(
        annotation: PdfAnnotation.Shape,
        box: PageBox,
    ): PDAnnotation =
        when (annotation.kind) {
            ShapeKind.RECT, ShapeKind.OVAL -> {
                val subtype =
                    if (annotation.kind == ShapeKind.RECT) {
                        PDAnnotationSquareCircle.SUB_TYPE_SQUARE
                    } else {
                        PDAnnotationSquareCircle.SUB_TYPE_CIRCLE
                    }
                PDAnnotationSquareCircle(subtype).apply {
                    borderStyle = borderStyle(annotation.strokeWidthPt)
                    rectangle = annotation.bounds.toPdRectangle(box)
                }
            }
            ShapeKind.LINE, ShapeKind.ARROW -> {
                PDAnnotationLine().apply {
                    val start = box.toPdfPoint(annotation.start)
                    val end = box.toPdfPoint(annotation.end)
                    setLine(floatArrayOf(start.x, start.y, end.x, end.y))
                    if (annotation.kind == ShapeKind.ARROW) {
                        endPointEndingStyle = PDAnnotationLine.LE_OPEN_ARROW
                    }
                    borderStyle = borderStyle(annotation.strokeWidthPt)
                    rectangle = annotation.bounds.toPdRectangle(box)
                }
            }
        }

    private fun buildNote(
        annotation: PdfAnnotation.Note,
        box: PageBox,
    ): PDAnnotationText =
        PDAnnotationText().apply {
            name = PDAnnotationText.NAME_COMMENT
            contents = annotation.contents
            rectangle = annotation.bounds.toPdRectangle(box)
        }

    private fun buildFreeText(
        annotation: PdfAnnotation.FreeText,
        box: PageBox,
    ): PDAnnotationMarkup {
        val freeText = PDAnnotationMarkup()
        freeText.cosObject.setName(COSName.SUBTYPE, PDAnnotationMarkup.SUB_TYPE_FREETEXT)
        freeText.contents = annotation.text
        val (r, g, b) = annotation.colorRgb.toRgbFloats()
        freeText.defaultAppearance = "$r $g $b rg /Helv ${annotation.fontSizePt} Tf"
        freeText.rectangle = annotation.rect.toPdRectangle(box)
        return freeText
    }

    private fun buildStamp(
        doc: PDDocument,
        annotation: PdfAnnotation.Stamp,
        box: PageBox,
    ): PDAnnotationRubberStamp {
        val stamp = PDAnnotationRubberStamp()
        stamp.rectangle = annotation.rect.toPdRectangle(box)
        val width = annotation.rect.width
        val height = annotation.rect.height
        val appearance = PDAppearanceStream(doc)
        appearance.bBox = PDRectangle(width, height)
        appearance.resources = PDResources()
        val image = PDImageXObject.createFromByteArray(doc, annotation.pngBytes, "salemstamp")
        PDPageContentStream(doc, appearance).use { content ->
            content.drawImage(image, 0f, 0f, width, height)
        }
        stamp.appearance = PDAppearanceDictionary().apply { setNormalAppearance(appearance) }
        return stamp
    }

    private fun parseAnnotation(
        annotation: PDAnnotation,
        name: String,
        pageIndex: Int,
        box: PageBox,
    ): PdfAnnotation? {
        val id = name
        val color = annotation.color.toRgbInt()
        val opacity = (annotation as? PDAnnotationMarkup)?.constantOpacity ?: 1f
        return when (annotation.subtype) {
            PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT, PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE,
            PDAnnotationTextMarkup.SUB_TYPE_STRIKEOUT,
            -> parseTextMarkup(annotation, id, pageIndex, color, opacity, box)
            PDAnnotationMarkup.SUB_TYPE_INK -> parseInk(annotation, id, pageIndex, color, opacity, box)
            PDAnnotationSquareCircle.SUB_TYPE_SQUARE, PDAnnotationSquareCircle.SUB_TYPE_CIRCLE ->
                parseSquareCircle(annotation, id, pageIndex, color, opacity, box)
            PDAnnotationLine.SUB_TYPE -> parseLine(annotation, id, pageIndex, color, opacity, box)
            PDAnnotationText.SUB_TYPE -> parseNote(annotation, id, pageIndex, color, opacity, box)
            PDAnnotationMarkup.SUB_TYPE_FREETEXT -> parseFreeText(annotation, id, pageIndex, color, opacity, box)
            PDAnnotationRubberStamp.SUB_TYPE -> parseStamp(annotation, id, pageIndex, color, opacity, box)
            else -> null
        }
    }

    private fun parseTextMarkup(
        annotation: PDAnnotation,
        id: String,
        pageIndex: Int,
        color: Int,
        opacity: Float,
        box: PageBox,
    ): PdfAnnotation.TextMarkup? {
        val markup = annotation as? PDAnnotationTextMarkup ?: return null
        val kind =
            when (markup.subtype) {
                PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT -> MarkupKind.HIGHLIGHT
                PDAnnotationTextMarkup.SUB_TYPE_UNDERLINE -> MarkupKind.UNDERLINE
                else -> MarkupKind.STRIKEOUT
            }
        val quads = markup.quadPoints ?: return null
        if (quads.size < 8) return null
        val rects =
            (quads.indices step 8).map { q ->
                val xs = floatArrayOf(quads[q], quads[q + 2], quads[q + 4], quads[q + 6])
                val ys = floatArrayOf(quads[q + 1], quads[q + 3], quads[q + 5], quads[q + 7])
                box.toDomainRect(xs.min(), ys.min(), xs.max(), ys.max())
            }
        return PdfAnnotation.TextMarkup(id, pageIndex, color, opacity, kind, rects)
    }

    private fun parseInk(
        annotation: PDAnnotation,
        id: String,
        pageIndex: Int,
        color: Int,
        opacity: Float,
        box: PageBox,
    ): PdfAnnotation.Ink? {
        val markup = annotation as? PDAnnotationMarkup ?: return null
        val inkList = markup.inkList ?: return null
        val strokes =
            inkList.mapNotNull { flat ->
                if (flat.size < 4) return@mapNotNull null
                (flat.indices step 2).map { i ->
                    box.toDomainPoint(flat[i], flat[i + 1])
                }
            }
        if (strokes.isEmpty()) return null
        val width = markup.borderStyle?.width ?: DEFAULT_STROKE_PT
        return PdfAnnotation.Ink(id, pageIndex, color, opacity, strokes, width)
    }

    private fun parseSquareCircle(
        annotation: PDAnnotation,
        id: String,
        pageIndex: Int,
        color: Int,
        opacity: Float,
        box: PageBox,
    ): PdfAnnotation.Shape? {
        val squareCircle = annotation as? PDAnnotationSquareCircle ?: return null
        val rect = squareCircle.rectangle ?: return null
        val width = squareCircle.borderStyle?.width ?: DEFAULT_STROKE_PT
        val pad = width / 2f
        // The appearance handler enlarges /Rect by the border width and records
        // the enlargement in /RD — inset it back out to recover our geometry.
        val differences = squareCircle.rectDifferences
        val hasDifferences = differences.size >= 4
        val llx = if (hasDifferences) rect.lowerLeftX + differences[0] else rect.lowerLeftX
        val lly = if (hasDifferences) rect.lowerLeftY + differences[3] else rect.lowerLeftY
        val urx = if (hasDifferences) rect.upperRightX - differences[2] else rect.upperRightX
        val ury = if (hasDifferences) rect.upperRightY - differences[1] else rect.upperRightY
        val domain = box.toDomainRect(llx, lly, urx, ury)
        val kind =
            if (squareCircle.subtype == PDAnnotationSquareCircle.SUB_TYPE_SQUARE) ShapeKind.RECT else ShapeKind.OVAL
        return PdfAnnotation.Shape(
            id = id,
            pageIndex = pageIndex,
            colorRgb = color,
            opacity = opacity,
            kind = kind,
            start = PointPt(domain.left + pad, domain.top + pad),
            end = PointPt(domain.right - pad, domain.bottom - pad),
            strokeWidthPt = width,
        )
    }

    private fun parseLine(
        annotation: PDAnnotation,
        id: String,
        pageIndex: Int,
        color: Int,
        opacity: Float,
        box: PageBox,
    ): PdfAnnotation.Shape? {
        val line = annotation as? PDAnnotationLine ?: return null
        val points = line.line ?: return null
        if (points.size < 4) return null
        val kind =
            if (line.endPointEndingStyle == PDAnnotationLine.LE_OPEN_ARROW) ShapeKind.ARROW else ShapeKind.LINE
        return PdfAnnotation.Shape(
            id = id,
            pageIndex = pageIndex,
            colorRgb = color,
            opacity = opacity,
            kind = kind,
            start = box.toDomainPoint(points[0], points[1]),
            end = box.toDomainPoint(points[2], points[3]),
            strokeWidthPt = line.borderStyle?.width ?: DEFAULT_STROKE_PT,
        )
    }

    private fun parseNote(
        annotation: PDAnnotation,
        id: String,
        pageIndex: Int,
        color: Int,
        opacity: Float,
        box: PageBox,
    ): PdfAnnotation.Note? {
        val rect = annotation.rectangle ?: return null
        val domain = box.toDomainRect(rect.lowerLeftX, rect.lowerLeftY, rect.upperRightX, rect.upperRightY)
        return PdfAnnotation.Note(
            id = id,
            pageIndex = pageIndex,
            colorRgb = color,
            opacity = opacity,
            at = PointPt(domain.left, domain.top),
            contents = annotation.contents.orEmpty(),
        )
    }

    private fun parseFreeText(
        annotation: PDAnnotation,
        id: String,
        pageIndex: Int,
        color: Int,
        opacity: Float,
        box: PageBox,
    ): PdfAnnotation.FreeText? {
        val markup = annotation as? PDAnnotationMarkup ?: return null
        val rect = annotation.rectangle ?: return null
        val fontSize =
            markup.defaultAppearance
                ?.let { FONT_SIZE_REGEX.find(it)?.groupValues?.getOrNull(1)?.toFloatOrNull() }
                ?: DEFAULT_FONT_SIZE_PT
        return PdfAnnotation.FreeText(
            id = id,
            pageIndex = pageIndex,
            colorRgb = color,
            opacity = opacity,
            rect = box.toDomainRect(rect.lowerLeftX, rect.lowerLeftY, rect.upperRightX, rect.upperRightY),
            text = annotation.contents.orEmpty(),
            fontSizePt = fontSize,
        )
    }

    private fun parseStamp(
        annotation: PDAnnotation,
        id: String,
        pageIndex: Int,
        color: Int,
        opacity: Float,
        box: PageBox,
    ): PdfAnnotation.Stamp? {
        val rect = annotation.rectangle ?: return null
        val appearance = annotation.normalAppearanceStream ?: return null
        val resources = appearance.resources ?: return null
        val imageName = resources.xObjectNames.firstOrNull() ?: return null
        val image = resources.getXObject(imageName) as? PDImageXObject ?: return null
        val bitmap = image.image ?: return null
        val png =
            ByteArrayOutputStream().let { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.toByteArray()
            }
        return PdfAnnotation.Stamp(
            id = id,
            pageIndex = pageIndex,
            colorRgb = color,
            opacity = opacity,
            rect = box.toDomainRect(rect.lowerLeftX, rect.lowerLeftY, rect.upperRightX, rect.upperRightY),
            pngBytes = png,
        )
    }

    private fun borderStyle(widthPt: Float): PDBorderStyleDictionary =
        PDBorderStyleDictionary().apply { width = widthPt }

    private fun sessionName(id: String): String =
        if (id.startsWith(SALEM_ANNOTATION_PREFIX)) id else SALEM_ANNOTATION_PREFIX + id.removePrefix("new:")

    companion object {
        const val DEFAULT_STROKE_PT = 2f
        const val DEFAULT_FONT_SIZE_PT = 12f
        private val FONT_SIZE_REGEX = Regex("""(\d+(?:\.\d+)?)\s+Tf""")
    }
}

private fun PDPage.pageBox(): PageBox {
    val crop = cropBox ?: mediaBox
    return PageBox(crop.lowerLeftX, crop.lowerLeftY, crop.width, crop.height, rotation)
}

private fun RectPt.toPdRectangle(box: PageBox): PDRectangle {
    val coords = box.toPdfRect(this)
    return PDRectangle(coords[0], coords[1], coords[2] - coords[0], coords[3] - coords[1])
}

private fun Int.toRgbFloats(): Triple<Float, Float, Float> =
    Triple(
        ((this shr 16) and 0xFF) / 255f,
        ((this shr 8) and 0xFF) / 255f,
        (this and 0xFF) / 255f,
    )

private fun Int.toPdColor(): PDColor {
    val (r, g, b) = toRgbFloats()
    return PDColor(floatArrayOf(r, g, b), PDDeviceRGB.INSTANCE)
}

private fun PDColor?.toRgbInt(): Int {
    val components = this?.components ?: return 0x000000
    if (components.size < 3) return 0x000000
    val r = (components[0] * 255f).toInt().coerceIn(0, 255)
    val g = (components[1] * 255f).toInt().coerceIn(0, 255)
    val b = (components[2] * 255f).toInt().coerceIn(0, 255)
    return (r shl 16) or (g shl 8) or b
}
