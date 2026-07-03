package app.salempdf

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class SalemPdfApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // PdfBox needs the asset loader for its bundled fonts (freetext appearances).
        PDFBoxResourceLoader.init(this)
    }
}
