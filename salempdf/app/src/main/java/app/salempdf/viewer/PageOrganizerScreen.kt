package app.salempdf.viewer

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.salempdf.R
import app.salempdf.domain.pageops.PageOp

/**
 * Page organizer: thumbnail grid with multi-select and the DOCOPS actions —
 * rotate, delete, move, insert, extract, merge (add PDF), compress.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageOrganizerScreen(
    vm: ViewerViewModel,
    pageCount: Int,
    title: String,
    onClose: () -> Unit,
) {
    var selected by remember { mutableStateOf(setOf<Int>()) }
    var confirmDelete by remember { mutableStateOf(false) }
    // Ops change indices; drop the selection whenever the document reloads.
    LaunchedEffect(pageCount) { selected = emptySet() }
    BackHandler(onBack = onClose)

    val mergeLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) vm.mergeWith(uri)
        }
    val extractLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            if (uri != null) {
                vm.extractTo(uri, selected.sorted())
                selected = emptySet()
            }
        }
    val compressLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
            if (uri != null) vm.compressTo(uri)
        }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    if (selected.isEmpty()) {
                        stringResource(R.string.pages_title)
                    } else {
                        stringResource(R.string.pages_selected, selected.size)
                    },
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.go_back))
                }
            },
        )
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(96.dp),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(count = pageCount, key = { it }) { page ->
                    PageCell(
                        vm = vm,
                        page = page,
                        selected = page in selected,
                        onTap = {
                            selected = if (page in selected) selected - page else selected + page
                        },
                    )
                }
            }
            if (vm.isWorking) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        OrganizerActions(
            vm = vm,
            pageCount = pageCount,
            selected = selected.sorted(),
            enabled = !vm.isWorking,
            handlers =
                OrganizerHandlers(
                    onDeleteRequest = { confirmDelete = true },
                    onExtract = { extractLauncher.launch(title.toExportName("extract")) },
                    onAddPdf = { mergeLauncher.launch(arrayOf("application/pdf")) },
                    onCompress = { compressLauncher.launch(title.toExportName("compressed")) },
                    onClearSelection = { selected = emptySet() },
                ),
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_pages_title, selected.size)) },
            text = { Text(stringResource(R.string.delete_pages_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        vm.applyPageOps(listOf(PageOp.Delete(selected.sorted())))
                    },
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

private fun String.toExportName(suffix: String): String {
    val base = removeSuffix(".pdf").removeSuffix(".PDF").ifBlank { "document" }
    return "$base-$suffix.pdf"
}

@Composable
private fun PageCell(
    vm: ViewerViewModel,
    page: Int,
    selected: Boolean,
    onTap: () -> Unit,
) {
    LaunchedEffect(page) { vm.renderer.requestThumbnail(page) }
    val bitmap = vm.renderer.thumbCache.get(vm.renderer.thumbKey(page))
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.77f)
                    .clip(RoundedCornerShape(6.dp))
                    .border(if (selected) 3.dp else 1.dp, borderColor, RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable(onClick = onTap),
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.page_cell_description, page + 1),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
            }
        }
        Text(
            text = (page + 1).toString(),
            style = MaterialTheme.typography.labelMedium,
            color =
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Launcher/dialog callbacks for the organizer action bar. */
private data class OrganizerHandlers(
    val onDeleteRequest: () -> Unit,
    val onExtract: () -> Unit,
    val onAddPdf: () -> Unit,
    val onCompress: () -> Unit,
    val onClearSelection: () -> Unit,
)

@Composable
private fun OrganizerActions(
    vm: ViewerViewModel,
    pageCount: Int,
    selected: List<Int>,
    enabled: Boolean,
    handlers: OrganizerHandlers,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        if (selected.isEmpty()) {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    TextButton(
                        enabled = enabled,
                        onClick = handlers.onAddPdf,
                    ) { Text(stringResource(R.string.add_pdf)) }
                }
                item {
                    TextButton(
                        enabled = enabled,
                        onClick = { vm.applyPageOps(listOf(PageOp.InsertBlank(at = pageCount))) },
                    ) {
                        Text(stringResource(R.string.insert_blank))
                    }
                }
                item {
                    TextButton(
                        enabled = enabled,
                        onClick = handlers.onCompress,
                    ) { Text(stringResource(R.string.compress)) }
                }
            }
        } else {
            androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                item {
                    TextButton(
                        enabled = enabled,
                        onClick = { vm.applyPageOps(listOf(PageOp.Rotate(selected))) },
                    ) {
                        Text(stringResource(R.string.rotate))
                    }
                }
                item {
                    TextButton(enabled = enabled, onClick = handlers.onDeleteRequest) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                }
                item {
                    TextButton(
                        enabled = enabled,
                        onClick = handlers.onExtract,
                    ) { Text(stringResource(R.string.extract)) }
                }
                if (selected.size == 1) {
                    val page = selected.first()
                    item {
                        TextButton(
                            enabled = enabled && page > 0,
                            onClick = { vm.applyPageOps(listOf(PageOp.Move(page, page - 1))) },
                        ) {
                            Text(stringResource(R.string.move_earlier))
                        }
                    }
                    item {
                        TextButton(
                            enabled = enabled && page < pageCount - 1,
                            onClick = { vm.applyPageOps(listOf(PageOp.Move(page, page + 1))) },
                        ) {
                            Text(stringResource(R.string.move_later))
                        }
                    }
                    item {
                        TextButton(
                            enabled = enabled,
                            onClick = { vm.applyPageOps(listOf(PageOp.InsertBlank(at = page + 1))) },
                        ) {
                            Text(stringResource(R.string.insert_blank))
                        }
                    }
                }
                item {
                    TextButton(enabled = enabled, onClick = handlers.onClearSelection) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}
