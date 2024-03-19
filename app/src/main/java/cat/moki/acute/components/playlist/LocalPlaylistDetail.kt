package cat.moki.acute.components.playlist

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.media3.common.MediaItem
import cat.moki.acute.AcuteApplication
import cat.moki.acute.components.LocalCacheData
import cat.moki.acute.models.CacheTrackFile


@Composable
fun LocalPlaylistDetail() {
    val (cacheFileList, cacheFileMap, servers, serverCacheTrackFileMap) = LocalCacheData()

    LazyColumn {
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