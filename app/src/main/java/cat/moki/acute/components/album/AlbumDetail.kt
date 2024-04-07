package cat.moki.acute.components.album

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Expand
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import cat.moki.acute.AcuteApplication
import cat.moki.acute.components.LibraryViewModelLocal
import cat.moki.acute.components.utils.AutoCoverPic
import cat.moki.acute.components.utils.DurationAndCount
import cat.moki.acute.components.utils.TitleBracketScale
import cat.moki.acute.models.MediaId
import cat.moki.acute.models.cacheInfo
import cat.moki.acute.models.duration
import cat.moki.acute.models.localMediaId
import cat.moki.acute.models.rawUrl
import cat.moki.acute.models.song
import cat.moki.acute.models.songs
import cat.moki.acute.services.TrackDownloadService

import kotlin.math.pow

@Composable
fun AlbumDetailComponent(albumId: MediaId) {

    val library = LibraryViewModelLocal.current
    var expand = remember { mutableStateOf(false) }

    val menuOpenIndex = remember { mutableIntStateOf(-1) }
    val duration = rememberSaveable { mutableLongStateOf(1L) }
    val albumItem = remember { mutableStateOf(library.cacheDetail.value, neverEqualPolicy()) }
    val songList = remember { mutableStateListOf<MediaItem>() }
    val scrollState = rememberScrollState(initial = 0)

    LaunchedEffect(albumId) {
        val a = library.getAlbumDetail(albumId)
        albumItem.value = a
        Log.d("TAG", "AlbumDetailComponent: ${a?.songs}")
        duration.longValue = library.detail.value?.duration ?: 1
    }

    LaunchedEffect(albumId) {
        songList.addAll(library.getAlbumSongList(albumId))
    }

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
    ) {
        albumItem.value?.let {
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val blurSize =
                    (16 * (1 - 1 / ((scrollState.value.toDouble() - screenHeight.value / 4.0).coerceAtLeast(0.0) / screenHeight.value + 1f).pow(
                        0.5
                    ))).dp
                AutoCoverPic(modifier = Modifier
                    .graphicsLayer { translationY = scrollState.value / 2f }
                    .blur(blurSize), albumId = albumId)
            }

            AlbumDetailHeadInfo(
                album = it,
                duration = duration.longValue,
                songCount = songList.size,
                scrollPosition = scrollState.value,
                expand
            )
        }

        if (songList.isEmpty()) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        songList.mapIndexed { index, song ->
            SongListItem(
                song = song,
                sameArtist = song.mediaMetadata.artist == albumItem.value?.mediaMetadata?.artist,
                menuOpenIndex = menuOpenIndex,
                index = index,
                expand
            )
            if (index + 1 != songList.size) {
                Row {
                    Divider(
                        modifier = Modifier.width(12.dp),
                        color = MaterialTheme.colorScheme.background
                    )
                    Divider(modifier = Modifier.weight(1f))
                    Divider(
                        modifier = Modifier.width(12.dp),
                        color = MaterialTheme.colorScheme.background
                    )

                }
            }


        }
    }
}


@Composable
fun AlbumTimeAndTracksInfo(ms: Long, tracksNumber: Int) {
    DurationAndCount(ms, tracksNumber)
}

@OptIn(UnstableApi::class)
@Composable
fun AlbumDetailHeadInfo(album: MediaItem, duration: Long, songCount: Int, scrollPosition: Int, expand: MutableState<Boolean>) {
    val TAG = "AlbumDetailHeadInfo"
    val context = LocalContext.current
    val artist = album.mediaMetadata.albumArtist
    val title = album.mediaMetadata.albumTitle

    Card(
        shape = RoundedCornerShape(bottomEnd = 0.dp, bottomStart = 0.dp),
        modifier = Modifier
            .padding(top = 8.dp)
            .shadow(scrollPosition.dp, clip = false)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(1f)) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.Start
                    ) {
                        TitleBracketScale(text = title.toString(), fontSize = 26.sp, lineHeight = 32.sp)
                        Text(text = artist.toString(), fontSize = 18.sp)
                        AlbumTimeAndTracksInfo(duration, songCount)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .align(Alignment.CenterVertically)
                    ) {
                        IconButton(onClick = { expand.value = !expand.value }) {
                            Icon(if (expand.value) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, "show menu")
                        }
                    }
                }
            }
            if (expand.value)
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = {
                        album.songs?.map { it.mediaItem }?.forEach {
                            DownloadService.sendAddDownload(
                                context, TrackDownloadService::class.java,
                                DownloadRequest.Builder(it.localMediaId.toString(), it.rawUrl.toUri()).build(), true
                            )
                            Log.d(TAG, "AlbumDetailHeadInfo: add ${it.localMediaId} to download")
                        }
                    }) { Icon(Icons.Outlined.Download, "download the album") }
                    IconButton(onClick = {
                        Log.d(TAG, "AlbumDetailHeadInfo: ${album.songs}")
                        album.songs?.map { it.mediaItem }?.forEach {
                            AcuteApplication.application.downloadManager.removeDownload(it.localMediaId.toString())
                            Log.d(TAG, "AlbumDetailHeadInfo: delete ${it.localMediaId} from download")
                        }
                    }) { Icon(Icons.Outlined.Delete, "delete downloaded") }

                }

        }
    }
}

