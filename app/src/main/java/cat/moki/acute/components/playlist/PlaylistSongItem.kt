package cat.moki.acute.components.playlist

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import cat.moki.acute.AcuteApplication
import cat.moki.acute.components.LibraryViewModelLocal
import cat.moki.acute.components.PlayerViewModelLocal
import cat.moki.acute.components.utils.AutoCoverPic
import cat.moki.acute.models.CacheTrackFile
import cat.moki.acute.models.MediaId
import cat.moki.acute.models.albumLocalMediaId
import cat.moki.acute.models.localMediaId

@Composable
fun PlaylistSongItem(modifier: Modifier = Modifier, song: MediaItem) {
    Log.d("TAG", "PlaylistSongItem: ${song.albumLocalMediaId}")
    val player = PlayerViewModelLocal.current
    ListItem(
        modifier = modifier
            .height(72.dp)
            .clickable {
                player.play(player.addMediaItem(song))
            },
        leadingContent = {
            AutoCoverPic(
                trackId = song.localMediaId,
                albumId = song.albumLocalMediaId
            )
        },
        headlineContent = { Text(song.mediaMetadata.title.toString()) },
        supportingContent = { Text(song.mediaMetadata.artist.toString()) }
    )
}

@Composable
fun PlaylistDetail(mediaId: MediaId) {
    val library = LibraryViewModelLocal.current
    val player = PlayerViewModelLocal.current
    Log.d("TAG", "PlaylistDetail: $mediaId")

    val playlist = remember { mutableStateOf<Map<MediaItem, List<MediaItem>>>(emptyMap()) }
    val playlistDetail = remember { mutableStateListOf<MediaItem>() }

    LaunchedEffect(mediaId) {
        playlist.value = library.getPlaylists(mediaId.serverId)
        playlistDetail.clear()
        val list = playlist.value.firstNotNullOfOrNull { (item, list) -> if (item.localMediaId == mediaId) list else null }
        list?.let { playlistDetail.addAll(it) }
    }
    Log.d("TAG", "PlaylistDetail: ${playlistDetail}")
    Log.d("TAG", "playlist: ${playlist}")

    Box(modifier = Modifier.fillMaxWidth()) {
        LazyColumn {
            itemsIndexed(playlistDetail) { i, mediaItem ->
                PlaylistSongItem(modifier = Modifier.clickable {
                    player.play(player.addMediaItem(mediaItem))
                }, mediaItem)
            }
        }

    }
}
