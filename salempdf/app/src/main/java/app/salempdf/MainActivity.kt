package app.salempdf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.salempdf.home.HomeScreen
import app.salempdf.ui.theme.SalemPdfTheme
import app.salempdf.viewer.ViewerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalemPdfTheme {
                SalemPdfRoot()
            }
        }
    }
}

private val UriSaver =
    Saver<Uri?, String>(
        save = { it?.toString() ?: "" },
        restore = { if (it.isEmpty()) null else Uri.parse(it) },
    )

@Composable
private fun SalemPdfRoot() {
    var openUri by rememberSaveable(stateSaver = UriSaver) { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val openLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                // Keep read access across restarts; some providers don't grant it.
                runCatching {
                    context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                openUri = uri
            }
        }

    val uri = openUri
    if (uri == null) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HomeScreen(
                onOpenDocument = { openLauncher.launch(arrayOf("application/pdf")) },
                modifier = Modifier.padding(innerPadding),
            )
        }
    } else {
        BackHandler { openUri = null }
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                ViewerScreen(uri = uri, onClose = { openUri = null })
            }
        }
    }
}
