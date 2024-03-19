package cat.moki.acute.components.album

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.NetClient
import cat.moki.acute.components.PlayerViewModelLocal
import cat.moki.acute.components.utils.TitleBracketScale
import cat.moki.acute.models.cacheInfo
import cat.moki.acute.models.serverId
import cat.moki.acute.services.LocalSimpleCache
import cat.moki.acute.utils.conditional


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListItem(
    song: MediaItem,
    sameArtist: Boolean,
    menuOpenIndex: MutableState<Int>,
    index: Int
) {
    val TAG = "SongListItem"
    val player = PlayerViewModelLocal.current
    val menuOpen = menuOpenIndex.value == index
    val touchPoint = remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    var textColor = MaterialTheme.typography.bodyLarge.color
    if (menuOpenIndex.value >= 0) {
        textColor = if (menuOpenIndex.value != index) {
            MaterialTheme.colorScheme.copy(surface = MaterialTheme.colorScheme.surface).surfaceColorAtElevation(96.dp)
        } else {
            textColor
        }
    }
    val cached = rememberSaveable { mutableStateOf(false) }
    if (!AcuteApplication.useInternet) {
        if (!cached.value) {
            textColor = MaterialTheme.colorScheme.copy(surface = MaterialTheme.colorScheme.surface).surfaceColorAtElevation(72.dp)
        }
    }


    LaunchedEffect(song) {
        Log.d(TAG, "SongListItem: ${song}")
        cached.value = AcuteApplication.fullyCached(song.cacheInfo)
        Log.d(TAG, "SongListItem: ${cached.value}")
    }

    Box(modifier = Modifier.conditional(menuOpen) {
        Modifier.shadow(elevation = 4.dp)
    }) {
        ListItem(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(0.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            touchPoint.value = it
                        }
                    )
                }
                .combinedClickable(
                    onClick = {
                        player.play(player.addMediaItem(mediaItem = song))
                    },
                    onLongClick = {
                        menuOpenIndex.value = index
                    }
                ),
            headlineContent = {
                TitleBracketScale(
                    text = song.mediaMetadata.title.toString(),
                    fontSize = 18.sp,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                if (!sameArtist)
                    Text(
                        text = song.mediaMetadata.artist.toString(),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
            },
            trailingContent = {
                if (cached.value)
                    Icon(Icons.Rounded.OfflinePin, "")
            },
        )
    }
    val (xDp, yDp) = with(density) {
        (touchPoint.value.x.toDp()) to (touchPoint.value.y.toDp())
    }
    if (menuOpen) Menu(menuOpenIndex, index, xDp, song)

}