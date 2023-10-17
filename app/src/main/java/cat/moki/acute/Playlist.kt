package cat.moki.acute

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.NoDragCancelledAnimation
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable


@Composable
fun PlayList(browser: MediaBrowser) {
    val state = rememberReorderableLazyListState(
        onMove = { from, to -> { TODO() } },
        dragCancelledAnimation = NoDragCancelledAnimation()
    )
    val scope = rememberCoroutineScope()
    if (browser.mediaItemCount == 0) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "¯\\_(ツ)_/¯", fontSize = 48.sp)
            Text(text = "Go back to library for some songs!")
            Spacer(modifier = Modifier.weight(1f))
        }
        return
    }
    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .reorderable(state)
            .fillMaxHeight()
    ) {
        for (i: Int in 0 until browser.mediaItemCount) {
            item {
                Text(browser.getMediaItemAt(i).mediaId)
            }
        }
    }
}