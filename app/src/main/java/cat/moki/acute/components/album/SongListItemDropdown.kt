package cat.moki.acute.components.album

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.NetClient
import cat.moki.acute.components.LibraryViewModelLocal
import cat.moki.acute.components.PlayerViewModelLocal
import cat.moki.acute.models.localMediaId
import cat.moki.acute.models.serverId
import cat.moki.acute.services.TrackDownloadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val MIN = 32.dp
val MOVE = 128.dp

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Menu(menuOpenIndex: MutableState<Int>, index: Int, xDp: Dp, song: MediaItem) {
    val TAG = "Menu"
    val library = LibraryViewModelLocal.current
    val player = PlayerViewModelLocal.current
    val context = LocalContext.current
    val defaultPlaylistId = AcuteApplication.application.defaultPlaylistMap.value[library.serverId.value]
    val scope = rememberCoroutineScope()
    val playlistDialogOpen = remember { mutableStateOf(false) }
    fun dismiss() {
        menuOpenIndex.value = -1
    }

    val x = if ((xDp - MOVE) > MIN) {
        (xDp - MOVE)
    } else MIN
    Box {
        DropdownMenu(
            expanded = menuOpenIndex.value == index,
            offset = DpOffset(x, 0.dp),
            onDismissRequest = { dismiss() }
        ) {
            DropdownMenuItem(
                text = { Text("Play") },
                onClick = {
                    dismiss()
                    player.seek(player.addMediaItem(song))
                    player.play()
                }
            )
            DropdownMenuItem(
                text = { Text("Add to last") },
                onClick = {
                    dismiss()
                    player.addMediaItem(song)
                }
            )
            DropdownMenuItem(
                text = { Text("Play next") },
                onClick = {
                    dismiss()
                    player.addMediaItem(song, player.currentMediaIndex.value ?: 0)
                }
            )

            Divider()
            if (defaultPlaylistId != null)
                DropdownMenuItem(
                    text = { Text("Add to default playlist") },
                    onClick = {
                        dismiss()
                        NetClient.request(serverId = library.serverId.value)
                        scope.launch(Dispatchers.IO) {
                            val response = NetClient.request(serverId = library.serverId.value)
                                .updatePlaylist(playlistId = defaultPlaylistId, addIds = listOf(song.mediaId))
                            if (!response.isSuccessful || response.body() == null || !response.body()!!.ok) {
                                Log.w("TAG", "Menu: Add to default playlist ${response.errorBody()}")
                                //TODO failed
                                return@launch
                            } else {
                                //TODO
                                return@launch
                            }

                        }

                    }
                )
            DropdownMenuItem(
                text = { Text("Add to...") },
                onClick = {
                    playlistDialogOpen.value = true
                }
            )
            DropdownMenuItem(
                text = { Text("Download") },
                onClick = {
                    dismiss()
                    DownloadService.sendAddDownload(
                        context, TrackDownloadService::class.java,
                        DownloadRequest.Builder(
                            song.localMediaId.toString(),
                            NetClient.link(song.serverId).getRawStreamUrl(song.mediaId).toUri(),
                        ).build(),
                        false
                    )

                }
            )
            if (playlistDialogOpen.value)
                ModalBottomSheet(
                    onDismissRequest = {
                        playlistDialogOpen.value = false
                        dismiss()
                    }
                ) {
                    val playlist = remember { mutableStateOf<Map<MediaItem, List<MediaItem>>>(emptyMap()) }
                    val playlistContains = remember { mutableStateMapOf<MediaItem, Boolean>() }

//                    val playlist = remember { mutableStateOf<Map<MediaItem, List<MediaItem>>?>(emptyMap()) }
                    LaunchedEffect(song) {
                        playlist.value = library.getPlaylists(song.serverId)
                        playlistContains.putAll(playlist.value.map { (p, list) ->
                            Pair(
                                p,
                                list.indexOfFirst { it.localMediaId == song.localMediaId } != -1
                            )
                        })
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 72.dp)
                            .padding(16.dp),
                    ) {
                        Column {
                            playlistContains.keys.map {
                                ListItem(
                                    headlineContent = {
                                        Text(it.mediaMetadata.title.toString())
                                    },
                                    trailingContent = {
                                        Checkbox(
                                            checked = playlistContains[it] ?: false,
                                            onCheckedChange = { value -> playlistContains[it] = value }
                                        )
                                    }
                                )
                            }
                        }
                    }

                }
        }
    }

}

