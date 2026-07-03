package app.salempdf.viewer

import android.app.Application
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import app.salempdf.domain.annotation.PdfAnnotation
import app.salempdf.domain.render.RectPt
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
    var showTools by rememberSaveable { mutableStateOf(false) }
    var placement by remember { mutableStateOf<PlacementRequest?>(null) }
    var showClosePrompt by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val requestClose = {
        if (vm.isDirty) showClosePrompt = true else onClose()
    }
    BackHandler(onBack = requestClose)

    LaunchedEffect(Unit) {
        vm.saveMessage.collect { message ->
            if (message != null) {
                snackbarHostState.showSnackbar(message)
                vm.consumeSaveMessage()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ViewerTopBar(
                vm = vm,
                ready = ready,
                currentPage = currentPage,
                onClose = requestClose,
                onShowJump = { showJumpDialog = true },
                onToggleTools = {
                    showTools = !showTools
                    if (!showTools) vm.setTool(null)
                },
            )
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
                        onPlacement = { request -> placement = request },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            if (showTools) {
                AnnotationToolbar(vm)
            }
            ThumbnailStrip(
                vm = vm,
                pageCount = ready.pageCount,
                currentPage = currentPage,
                onPageTap = { page -> transform.scrollTo(layout.pageTopPx(page), layout, viewport) },
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    PlacementDialogs(vm, placement, onDone = { placement = null })

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

    if (showClosePrompt) {
        AlertDialog(
            onDismissRequest = { showClosePrompt = false },
            title = { Text(stringResource(R.string.unsaved_title)) },
            text = { Text(stringResource(R.string.unsaved_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClosePrompt = false
                        vm.save()
                    },
                ) {
                    Text(stringResource(R.string.save_changes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClosePrompt = false
                        onClose()
                    },
                ) {
                    Text(stringResource(R.string.discard))
                }
            },
        )
    }
}

/** Dialog flows for tap-to-place tools: note text, freetext, signature capture. */
@Composable
private fun PlacementDialogs(
    vm: ViewerViewModel,
    placement: PlacementRequest?,
    onDone: () -> Unit,
) {
    if (placement == null) return
    when (placement.tool) {
        AnnotationTool.NOTE ->
            AnnotationTextDialog(
                title = stringResource(R.string.add_note),
                onDismiss = onDone,
                onConfirm = { text ->
                    vm.addAnnotation(
                        PdfAnnotation.Note(
                            id = vm.newAnnotationId(),
                            pageIndex = placement.pageIndex,
                            colorRgb = vm.toolColor,
                            opacity = 1f,
                            at = placement.point,
                            contents = text,
                        ),
                    )
                    onDone()
                },
            )
        AnnotationTool.FREETEXT ->
            AnnotationTextDialog(
                title = stringResource(R.string.add_text),
                onDismiss = onDone,
                onConfirm = { text ->
                    vm.addAnnotation(
                        PdfAnnotation.FreeText(
                            id = vm.newAnnotationId(),
                            pageIndex = placement.pageIndex,
                            colorRgb = vm.toolColor,
                            opacity = 1f,
                            rect =
                                RectPt(
                                    placement.point.x,
                                    placement.point.y,
                                    placement.point.x + FREETEXT_WIDTH_PT,
                                    placement.point.y + FREETEXT_HEIGHT_PT,
                                ),
                            text = text,
                            fontSizePt = FREETEXT_FONT_PT,
                        ),
                    )
                    onDone()
                },
            )
        AnnotationTool.SIGNATURE -> {
            val existing = vm.signaturePng
            if (existing != null) {
                placeSignature(vm, placement, existing)
                onDone()
            } else {
                SignatureDialog(
                    onDismiss = onDone,
                    onDone = { png ->
                        vm.signaturePng = png
                        placeSignature(vm, placement, png)
                        onDone()
                    },
                )
            }
        }
        else -> onDone()
    }
}

private const val FREETEXT_WIDTH_PT = 240f
private const val FREETEXT_HEIGHT_PT = 48f
private const val FREETEXT_FONT_PT = 14f
private const val SIGNATURE_WIDTH_PT = 150f

private fun placeSignature(
    vm: ViewerViewModel,
    placement: PlacementRequest,
    png: ByteArray,
) {
    val options = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
    android.graphics.BitmapFactory.decodeByteArray(png, 0, png.size, options)
    val aspect =
        if (options.outWidth > 0 && options.outHeight > 0) {
            options.outHeight.toFloat() / options.outWidth.toFloat()
        } else {
            0.4f
        }
    val height = SIGNATURE_WIDTH_PT * aspect
    vm.addAnnotation(
        PdfAnnotation.Stamp(
            id = vm.newAnnotationId(),
            pageIndex = placement.pageIndex,
            colorRgb = 0,
            opacity = 1f,
            rect =
                RectPt(
                    placement.point.x,
                    placement.point.y,
                    placement.point.x + SIGNATURE_WIDTH_PT,
                    placement.point.y + height,
                ),
            pngBytes = png,
        ),
    )
}

@Composable
private fun AnnotationTextDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                minLines = 2,
            )
        },
        confirmButton = {
            TextButton(enabled = input.isNotBlank(), onClick = { onConfirm(input) }) {
                Text(stringResource(R.string.done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerTopBar(
    vm: ViewerViewModel,
    ready: ViewerUiState.Ready,
    currentPage: Int,
    onClose: () -> Unit,
    onShowJump: () -> Unit,
    onToggleTools: () -> Unit,
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
            if (vm.isDirty) {
                TextButton(onClick = vm::save) {
                    Text(stringResource(R.string.save_changes))
                }
            }
            IconButton(onClick = onToggleTools) {
                Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.annotate))
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
