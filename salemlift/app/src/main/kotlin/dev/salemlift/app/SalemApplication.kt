package dev.salemlift.app

import android.app.Application
import dev.salemlift.app.di.AppContainer

/** Application entry point owning the manual-DI [AppContainer] (ARCHITECTURE §4). */
class SalemApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
