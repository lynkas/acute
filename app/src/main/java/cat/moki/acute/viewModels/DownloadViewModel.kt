package cat.moki.acute.viewModels

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import cat.moki.acute.AcuteApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.IOException

class DownloadViewModel : ViewModel() {
    val TAG = "DownloadViewModel"

    @UnstableApi
    val manager = AcuteApplication.application.downloadManager

    @OptIn(UnstableApi::class)
    val flow: Flow<DownloadEvent> = callbackFlow {
        val listener = object : DownloadManager.Listener {
            override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
                trySend(DownloadEvent.DownloadUpdate(download))
                trySend(DownloadEvent.Downloads(downloadManager.currentDownloads))
                try {
                    trySend(DownloadEvent.DownloadHistory(getDownloadHistory()))
                } catch (e: IOException) {
                    Log.w(TAG, "onDownloadChanged: ", e)
                }
            }

            override fun onDownloadsPausedChanged(downloadManager: DownloadManager, downloadsPaused: Boolean) {
                trySend(DownloadEvent.Paused(downloadsPaused))
            }

            override fun onDownloadRemoved(downloadManager: DownloadManager, download: Download) {
                trySend(DownloadEvent.DownloadRemoved(download))
                trySend(DownloadEvent.Downloads(downloadManager.currentDownloads))
            }
        }

        manager.addListener(listener)
        awaitClose { manager.removeListener(listener) }
    }

    @OptIn(UnstableApi::class)
    suspend fun getCurrentProgressDownload(id: String): Flow<Float?> {

        return callbackFlow {
            var close = false
            do {
                val percent = manager.currentDownloads.find { it.request.id == id }?.percentDownloaded
                Log.d(TAG, "getCurrentProgressDownload: $id $percent")

                trySend(percent).isSuccess
                withContext(Dispatchers.IO) {
                    delay(50)
                    Log.d(TAG, "getCurrentProgressDownload: $id sleep")
                }
                Log.d(TAG, "getCurrentProgressDownload: $id after sleep")
            } while (percent != null && percent < 100 && !close)
            awaitClose {
                Log.d(TAG, "getCurrentProgressDownload: $id closed")
                close = true
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun getDownloadHistory(): List<Download> {
        val list = mutableListOf<Download>()
        val cursor = manager.downloadIndex.getDownloads(Download.STATE_COMPLETED, Download.STATE_FAILED, Download.STATE_STOPPED)
        for (i in 0 until cursor.count) {
            cursor.moveToNext()
            list.add(0, cursor.download)
        }
        return list
    }
}