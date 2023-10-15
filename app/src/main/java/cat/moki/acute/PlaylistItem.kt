package cat.moki.acute

import android.util.Log
import android.view.KeyEvent.ACTION_DOWN
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorderAfterLongPress


@OptIn(ExperimentalComposeUiApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun PlaylistItem(
    playIt: () -> Unit,
    playing: Boolean,
    mediaItem: MediaItem,
    onMove: Boolean,
    someoneOnMove: Boolean,
    orderingState: ReorderableLazyListState,
) {
    var deleteShow by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .height(72.dp)
            .background(Color.White)
            .clickable(onClick = {
                playIt()
            })
            .pointerInteropFilter {
                deleteShow = it.action == ACTION_DOWN
                false
            }
            .pointerInput(Unit) {
                forEachGesture {
                    awaitPointerEventScope {
                        awaitFirstDown()

                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { pointerInputChange ->
                                deleteShow = pointerInputChange.pressed

                                Log.d("PlaylistItem: ", pointerInputChange.toString())

                            }
                        } while (event.changes.any { it.pressed })
                    }
                }

            }
            .then(if (someoneOnMove && !onMove) Modifier.alpha(0.95f) else Modifier.alpha(1f))
    ) {
        Row(Modifier.background(MaterialTheme.colorScheme.surface)) {
            ListItem(
                modifier = Modifier
                    .height(72.dp)
                    .weight(1f),
                colors = if (playing) ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
                else ListItemDefaults.colors(),
                leadingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(end = 8.dp)
                                .detectReorderAfterLongPress(orderingState),
                            imageVector = if (playing) Icons.Filled.PlayArrow else Icons.Filled.Menu,
                            contentDescription = ""
                        )

                        Card(shape = RoundedCornerShape(8.dp)) {

                            GlideImage(
                                model = mediaItem.mediaMetadata.artworkUri,
                                contentDescription = "",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f),
                            ) {
                                it
                                    .error(R.drawable.ic_baseline_library_music_24)
                                    .placeholder(R.drawable.ic_baseline_downloading_24)
                            }

                        }
                    }
                },
                headlineContent = {
                    Text(
                        text = mediaItem.mediaMetadata.title.toString(),
                        fontSize = 20.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                supportingContent = {
                    Text(
                        text = mediaItem.mediaMetadata.artist.toString(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(if (deleteShow) MaterialTheme.colorScheme.onError else Color.Transparent)
            )
        }
    }
}