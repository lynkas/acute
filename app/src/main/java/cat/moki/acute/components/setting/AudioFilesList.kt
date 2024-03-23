package cat.moki.acute.components.setting

import android.text.format.Formatter
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.LocalClient
import cat.moki.acute.components.LocalCacheData
import cat.moki.acute.models.CacheTrackFile
import cat.moki.acute.models.Song
import cat.moki.acute.services.LocalSimpleCache
import java.io.File
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.math.log

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun AudioFilesList() {
    val TAG = "AudioFilesList"
    val context = LocalContext.current
    val (cacheFileList, cacheFileMap, servers, serverCacheTrackFileMap, updateList) = LocalCacheData(true)

    LazyColumn {
        items(cacheFileList.value) { cacheFile ->
            val mediaItem = cacheFileMap.value[cacheFile]
            val fileSize = remember {
                mutableStateOf<String>(
                    Formatter.formatShortFileSize(
                        context,
                        LocalSimpleCache.cache.getCachedBytes(cacheFile.pathWithoutDownload, 0, Long.MAX_VALUE)
                    )
                )
            }
            val finished = remember { mutableStateOf(AcuteApplication.fullyCached(cacheFile)) }


            ListItem(
                headlineContent = {
                    if (mediaItem != null) Text(mediaItem.mediaMetadata.title.toString()) else Text(cacheFile.trackId)
                },
                supportingContent = {
                    if (mediaItem != null) Text(
                        "${fileSize.value} " +
                                (if (cacheFile.format != null) "${cacheFile.format} " else "") +
                                (if (cacheFile.bitrate != 0) "${cacheFile.bitrate}kbps " else "") +
                                (if (!finished.value) "Not fully cached" else "")
                    )
                },
                trailingContent = {
                    IconButton(onClick = {
                        LocalSimpleCache.cache.removeResource(cacheFile.pathWithoutDownload)
                        updateList()
                    }) {
                        Icon(Icons.Outlined.Close, "delete")
                    }
                }
            )

        }
    }
}