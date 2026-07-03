package app.salempdf.viewer

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.salempdf.R
import app.salempdf.domain.viewer.DocumentLayout
import kotlinx.coroutines.launch

private val PAGE_GAP = 12.dp

@Composable
fun ViewerScreen(
    uri: Uri,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val vm: ViewerViewModel =
        viewModel(
            key = uri.toString(),
            factory = ViewerViewModel.factory(context.applicationContext as Application, uri),
        )
    when (val state = vm.uiState.collectAsState().value) {
        is ViewerUiState.Loading -> LoadingState()
        is ViewerUiState.Failed -> ErrorState(state.message, onClose)
        is ViewerUiState.Ready -> ViewerContent(vm, state, onClose)
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(
    message: String,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.open_error_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        TextButton(onClick = onClose, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.go_back))
        }
    }
}

@Composable
private fun ViewerContent(
    vm: ViewerViewModel,
    ready: ViewerUiState.Ready,
    onClose: () -> Unit,
) {
    val transform = remember { ViewerTransform() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val gapPx = with(LocalDensity.current) { PAGE_GAP.toPx() }

    val layout by remember(canvasSize, gapPx) {
        derivedStateOf {
            DocumentLayout(
                pageSizes = vm.pageSizes.toList(),
                viewportWidthPx = canvasSize.width.toFloat().coerceAtLeast(1f),
                zoom = transform.zoom,
                gapPx = gapPx,
            )
        }
    }
    val viewport = Size(canvasSize.width.toFloat(), canvasSize.height.toFloat())
    val currentPage by remember {
        derivedStateOf {
            if (layout.pageCount == 0) 0 else layout.pageAtY(transform.scrollY + viewport.height * 0.3f)
        }
    }
    var showJumpDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        ViewerTopBar(vm, ready, currentPage, onClose, onShowJump = { showJumpDialog = true })
        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { canvasSize = it },
        ) {
            if (canvasSize != IntSize.Zero) {
                PdfCanvas(
                    viewModel = vm,
                    transform = transform,
                    layoutProvider = { layout },
                    viewport = viewport,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        ThumbnailStrip(
            vm = vm,
            pageCount = ready.pageCount,
            currentPage = currentPage,
            onPageTap = { page -> transform.scrollTo(layout.pageTopPx(page), layout, viewport) },
        )
    }

    if (showJumpDialog) {
        JumpToPageDialog(
            pageCount = ready.pageCount,
            onDismiss = { showJumpDialog = false },
            onJump = { page ->
                showJumpDialog = false
                transform.scrollTo(layout.pageTopPx(page), layout, viewport)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerTopBar(
    vm: ViewerViewModel,
    ready: ViewerUiState.Ready,
    currentPage: Int,
    onClose: () -> Unit,
    onShowJump: () -> Unit,
) {
    val selection by vm.selection.collectAsState()
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    TopAppBar(
        title = {
            Text(ready.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
            }
        },
        actions = {
            if (selection != null) {
                TextButton(
                    onClick = {
                        scope.launch {
                            val text = vm.selectedText()
                            if (text.isNotEmpty()) clipboard.setText(AnnotatedString(text))
                            vm.setSelection(null)
                        }
                    },
                ) {
                    Text(stringResource(R.string.copy))
                }
            }
            TextButton(onClick = onShowJump) {
                Text(stringResource(R.string.page_indicator, currentPage + 1, ready.pageCount))
            }
        },
    )
}

@Composable
private fun JumpToPageDialog(
    pageCount: Int,
    onDismiss: () -> Unit,
    onJump: (Int) -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val page = input.toIntOrNull()?.minus(1)?.takeIf { it in 0 until pageCount }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.go_to_page)) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text(stringResource(R.string.page_number_hint, pageCount)) },
            )
        },
        confirmButton = {
            TextButton(enabled = page != null, onClick = { page?.let(onJump) }) {
                Text(stringResource(R.string.go))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
