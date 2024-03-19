package cat.moki.acute.components.playlist

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import cat.moki.acute.AcuteApplication
import cat.moki.acute.Const
import cat.moki.acute.components.NavControllerLocal
import cat.moki.acute.components.utils.DurationAndCount
import cat.moki.acute.models.localMediaId
import cat.moki.acute.models.serverId
import cat.moki.acute.routes.Strings

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistListItem(playlist: MediaItem) {

    val navController = NavControllerLocal.current

    val duration = rememberSaveable {
        mutableLongStateOf(playlist.mediaMetadata.extras?.getLong(Const.Duration) ?: 0)
    }

    val songCount = rememberSaveable {
        mutableIntStateOf(playlist.mediaMetadata.extras?.getInt(Const.Count) ?: 0)
    }

    ListItem(
        modifier = Modifier.combinedClickable(
            onClick = { navController.navigate(Strings.PlaylistDetail.replace("{${Strings.Id}}", playlist.localMediaId.toString())) }
        ),
        supportingContent = {
            DurationAndCount(duration.longValue, songCount.intValue)
        },
        headlineContent = { Text(playlist.mediaMetadata.title.toString()) },
        trailingContent = {
            Log.d("TAG", "PlaylistListItem: ${playlist.serverId}")
            Log.d("TAG", "PlaylistListItem: ${playlist.mediaId}")
            Log.d("TAG", "PlaylistListItem: ${AcuteApplication.application.defaultPlaylistMap.value[playlist.serverId]}")
            val isThis = AcuteApplication.application.defaultPlaylistMap.value[playlist.serverId] == playlist.mediaId

            IconButton(onClick = {
                AcuteApplication.application.setDefaultPlaylist(playlist.serverId, playlist.mediaId)
            }) {
                Icon(if (isThis) Icons.Filled.Star else Icons.Outlined.StarBorder, contentDescription = "edit")
            }
        }
    )
}