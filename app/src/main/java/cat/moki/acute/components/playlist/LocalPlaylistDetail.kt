package cat.moki.acute.components.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import cat.moki.acute.AcuteApplication
import cat.moki.acute.components.LocalCacheData
import cat.moki.acute.components.PlayerViewModelLocal
import cat.moki.acute.models.CacheTrackFile


@Composable
fun LocalPlaylistDetail() {
    val (cacheFileList, cacheFileMap, servers, serverCacheTrackFileMap) = LocalCacheData()
    val player = PlayerViewModelLocal.current

    LazyColumn {
        item {
            ListItem(headlineContent = { Text(text = "All all to playlist") }, modifier = Modifier.clickable {
                servers.value.forEach { serverId ->
                    serverCacheTrackFileMap.value[serverId]!!.map {
                        if (cacheFileMap.value[it] == null) return@map
                        player.addMediaItem(cacheFileMap.value[it]!!)
                    }
                }
            })
        }

        servers.value.forEach { serverId ->

            item {
                val server = remember { mutableStateOf(serverId) }
                LaunchedEffect(serverId) {
                    val credential = AcuteApplication.application.serverMap[serverId]
                    if (credential != null) server.value = credential.displayName
                }

                ListItem(headlineContent = { Text(server.value) })
            }
            serverCacheTrackFileMap.value[serverId]!!.map {
                if (cacheFileMap.value[it] == null) return@map
                item {
                    PlaylistSongItem(song = cacheFileMap.value[it]!!)
                }
            }
        }

    }

}