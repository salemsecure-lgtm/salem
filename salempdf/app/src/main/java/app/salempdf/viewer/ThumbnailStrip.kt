package app.salempdf.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

private val STRIP_HEIGHT = 92.dp
private val THUMB_WIDTH = 60.dp

@Composable
fun ThumbnailStrip(
    vm: ViewerViewModel,
    pageCount: Int,
    currentPage: Int,
    onPageTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(currentPage) {
        listState.animateScrollToItem(currentPage.coerceAtLeast(0), scrollOffset = -200)
    }
    LazyRow(
        state = listState,
        modifier =
            modifier
                .fillMaxWidth()
                .height(STRIP_HEIGHT)
                .background(MaterialTheme.colorScheme.surfaceContainer),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        items(count = pageCount, key = { it }) { page ->
            ThumbnailItem(vm, page, selected = page == currentPage, onTap = { onPageTap(page) })
        }
    }
}

@Composable
private fun ThumbnailItem(
    vm: ViewerViewModel,
    page: Int,
    selected: Boolean,
    onTap: () -> Unit,
) {
    LaunchedEffect(page) { vm.requestThumbnail(page) }
    val bitmap = vm.thumbCache.get(vm.thumbKey(page))
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier =
            Modifier
                .padding(horizontal = 4.dp)
                .width(THUMB_WIDTH)
                .height(STRIP_HEIGHT - 16.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(if (selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Text(
                text = (page + 1).toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
