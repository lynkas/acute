package cat.moki.acute.components.player

import android.os.VibrationEffect
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.NetClient
import cat.moki.acute.components.PlayerViewModelLocal
import cat.moki.acute.components.utils.AutoCoverPic
import cat.moki.acute.components.utils.getVibrator
import cat.moki.acute.models.albumLocalMediaId
import cat.moki.acute.models.localMediaId
import cat.moki.acute.models.serverId
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistItem(
    song: MediaItem,
    realIndex: Int,
    updatePlaylist: (i: Int) -> Unit,
    menuMode: Boolean,
    changeMenuMode: (open: Boolean) -> Unit

) {
    var textColor = MaterialTheme.typography.bodyLarge.color

    val player = PlayerViewModelLocal.current
    val context = LocalContext.current
    val cached = rememberSaveable { mutableStateOf(song.mediaMetadata.isPlayable ?: false) }
    if (!AcuteApplication.useInternet) {
        if (!cached.value) {
            textColor = MaterialTheme.colorScheme.copy(surface = MaterialTheme.colorScheme.surface).surfaceColorAtElevation(72.dp)
        }
    }
    val vibrator = getVibrator(context)

    LaunchedEffect(song) {
        cached.value = song.mediaMetadata.isPlayable ?: false
    }

    Box(
        modifier = Modifier
            .height(72.dp)
            .clip(shape = RoundedCornerShape(0))
            .combinedClickable(onClick = {
                if (!menuMode) {
                    Log.d("TAG", "PlayList: seek to $realIndex")
                    player.seek(index = realIndex, time = 0)
                    player.play()
                } else {
                    changeMenuMode(false)
                }
            }, onLongClick = {
                vibrator.vibrate(VibrationEffect.createOneShot(20L, 220))
                changeMenuMode(true)
            })
    ) {
        ListItem(
            modifier = Modifier.height(72.dp),
            leadingContent = {
                AutoCoverPic(
                    trackId = song.localMediaId,
                    albumId = song.albumLocalMediaId
                )
            },
            headlineContent = {
                Text(
                    text = song.mediaMetadata.title.toString(),
                    fontSize = 20.sp,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = song.mediaMetadata.artist.toString(),
                    maxLines = 2,
                    color = textColor,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingContent = {
                if (menuMode)
                    Row {
                        IconButton(onClick = {
                            updatePlaylist(realIndex)
                            changeMenuMode(false)

                        }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = { changeMenuMode(false) }) {
                            Icon(Icons.Outlined.Close, contentDescription = "")
                        }
                    }
            }
        )


    }

}