package cat.moki.acute.components.download

import android.text.format.Formatter
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadService
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.LocalClient
import cat.moki.acute.components.DownloadViewModelLocal
import cat.moki.acute.components.utils.RoundProgressButton
import cat.moki.acute.models.MediaId
import cat.moki.acute.models.Song
import cat.moki.acute.models.toMediaId
import cat.moki.acute.services.TrackDownloadService
import cat.moki.acute.viewModels.DownloadEvent
import kotlinx.coroutines.launch
import java.io.IOException

@OptIn(UnstableApi::class)
@Composable
fun DownloadPage() {
    val TAG = "DownloadPage"
    val downloadViewModel = DownloadViewModelLocal.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = AcuteApplication.application.downloadManager

    var downloadList by remember { mutableStateOf<List<Download>>(manager.currentDownloads) }
    var downloadHistoryList by remember { mutableStateOf(emptyList<Download>()) }
    var downloadPaused by remember { mutableStateOf(manager.downloadsPaused) }

    LaunchedEffect(true) {
        try {
            downloadHistoryList = downloadViewModel.getDownloadHistory()
        } catch (e: IOException) {
            Log.w(TAG, "DownloadPage: ", e)
        }
    }
    val songs = remember { mutableStateMapOf<String, Song?>() }

    fun getSong(id: String): Song? {
        val mediaId = id.toMediaId()
        if (!songs.contains(id)) {
            val song = LocalClient(context = context, mediaId.serverId).getSong(mediaId.itemId)
            songs[id] = song
        }
        return songs[id]

    }

    scope.launch {
        downloadViewModel.flow.collect {
            when (it) {
                is DownloadEvent.Downloads -> downloadList = it.list
                is DownloadEvent.Paused -> downloadPaused = it.paused
                is DownloadEvent.DownloadUpdate -> downloadList = downloadList.map { d ->
                    if (d.request.id == it.download.request.id) it.download else d
                }

                is DownloadEvent.DownloadRemoved -> downloadList = downloadList.filter { d ->
                    d.request.id != it.download.request.id
                }

                is DownloadEvent.DownloadHistory -> downloadHistoryList = it.list


            }
        }
    }


    LazyColumn {
        itemsIndexed(downloadList, key = { i, download -> download.request.id }) { index, item ->
            var progress by rememberSaveable { mutableFloatStateOf(0f) }
            LaunchedEffect(item) {
                scope.launch {
                    downloadViewModel.getCurrentProgressDownload(item.request.id).collect {
                        progress = it?.div(100)?.coerceAtLeast(progress) ?: -1f
                    }
                }
            }
            DownloadItem(item, getSong(item.request.id), progress)
        }
        item(key = "download label") { ListItem(headlineContent = { Text("Download History") }) }
        itemsIndexed(downloadHistoryList, key = { i, download -> "d/" + download.request.id }) { index, item ->
            DownloadItem(
                item,
                getSong(item.request.id)
            )
        }


    }
}

@OptIn(UnstableApi::class)
@Composable
fun DownloadItem(item: Download, song: Song?, progress: Float = -1f) {
    val TAG = "DownloadItem"
    val downloadViewModel = DownloadViewModelLocal.current

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    val itemSizeString = Formatter.formatFileSize(
        context,
        item.contentLength
    )

    ListItem(
        headlineContent = { Text(song?.title ?: "UNKNOWN SONG") },
        supportingContent = {
            val downloadingText =
                "${Formatter.formatFileSize(context, (item.contentLength * progress).toLong())}/$itemSizeString (${"%.2f".format(progress * 100)}%)"

            Text(
                when (item.state) {
                    Download.STATE_QUEUED -> "Queued"
                    Download.STATE_FAILED -> "Download Error"
                    Download.STATE_COMPLETED -> itemSizeString
                    Download.STATE_DOWNLOADING -> if (progress <= 0) "Preparing" else downloadingText
                    else -> "State: ${item.state}"
                }
            )

        },
        trailingContent = {
            RoundProgressButton(modifier = Modifier.height(48.dp), value = progress) {
                if (item.state == Download.STATE_DOWNLOADING)
                    IconButton(onClick = {
                        DownloadService.sendRemoveDownload(context, TrackDownloadService::class.java, item.request.id, true)
                    }) { Icon(Icons.Outlined.Close, "delete") }

            }
        }
    )
}