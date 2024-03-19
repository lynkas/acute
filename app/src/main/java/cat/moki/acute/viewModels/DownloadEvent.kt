package cat.moki.acute.viewModels

import androidx.media3.exoplayer.offline.Download

sealed class DownloadEvent {
    data class Downloads(val list: List<Download>) : DownloadEvent()
    data class Paused(val paused: Boolean) : DownloadEvent()
    data class DownloadUpdate(val download: Download) : DownloadEvent()
    data class DownloadRemoved(val download: Download) : DownloadEvent()
    data class DownloadHistory(val list: List<Download>) : DownloadEvent()
}