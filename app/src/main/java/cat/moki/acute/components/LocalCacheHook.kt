package cat.moki.acute.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.media3.common.MediaItem
import cat.moki.acute.AcuteApplication
import cat.moki.acute.models.CacheTrackFile


data class Quad<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
typealias CacheFileResult = Quad<
        MutableState<List<CacheTrackFile>>,
        MutableState<Map<CacheTrackFile, MediaItem?>>,
        MutableState<List<String>>,
        MutableState<Map<String, List<CacheTrackFile>>>,
            () -> Unit>


@Composable
fun LocalCacheData(includeUnfinished: Boolean = false): CacheFileResult {
    val cacheFileList = remember { mutableStateOf<List<CacheTrackFile>>(emptyList()) }
    val cacheFileMap = remember { mutableStateOf<Map<CacheTrackFile, MediaItem?>>(emptyMap()) }
    val servers = remember { mutableStateOf<List<String>>(listOf()) }
    val serverCacheTrackFileMap = remember { mutableStateOf<Map<String, List<CacheTrackFile>>>(emptyMap()) }
    val version = remember { mutableIntStateOf(0) }

    LaunchedEffect(includeUnfinished, version.value) {
        cacheFileList.value =
            if (includeUnfinished) AcuteApplication.application.allCacheFiles() else AcuteApplication.application.allCacheFinishedFiles()
        cacheFileMap.value = AcuteApplication.application.allCacheFileMediaItem(cacheFileList.value)
        servers.value = cacheFileList.value.map { it.serverId }.distinct()
        serverCacheTrackFileMap.value = servers.value.associateWith { serverId -> cacheFileList.value.filter { it.serverId == serverId } }
    }
    return Quad(cacheFileList, cacheFileMap, servers, serverCacheTrackFileMap) {
        version.value += 1
    }
}