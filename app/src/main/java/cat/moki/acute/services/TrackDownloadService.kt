package cat.moki.acute.services

import android.app.Notification
import android.content.Context
import android.util.Log
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.DefaultDownloaderFactory
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import cat.moki.acute.AcuteApplication
import cat.moki.acute.R
import java.lang.RuntimeException
import java.util.concurrent.Executors


private val DownloadChannel = "download";

@UnstableApi
class TrackDownloadService : androidx.media3.exoplayer.offline.DownloadService(
    1,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DownloadChannel,
    R.string.app_name,
    R.string.app_name
) {
    private val TAG = "DownloadService"
    private val notificationHelper: DownloadNotificationHelper by lazy {
        DownloadNotificationHelper(this, DownloadChannel)
    }


    private val dataSource: DataSource.Factory by lazy {
        cacheDataSource(this)
    }


    override fun onCreate() {
        super.onCreate()

    }

    override fun getDownloadManager(): DownloadManager {
        return AcuteApplication.application.downloadManager
    }

    override fun getScheduler(): Scheduler {
        return PlatformScheduler(this, 1)
    }

    override fun getForegroundNotification(downloads: MutableList<Download>, notMetRequirements: Int): Notification {
        Log.d(TAG, "getForegroundNotification: ${downloads.size}")
        return notificationHelper.buildProgressNotification(
            this,
            R.drawable.ic_baseline_downloading_24,
            null,
            "${downloads.size - 1} to go",
            downloads,
            notMetRequirements
        )
    }


    private class TerminalStateNotificationHelper(
        context: Context, private val notificationHelper: DownloadNotificationHelper, private var nextNotificationId: Int
    ) : DownloadManager.Listener {
        private val context: Context

        private var failedNumber = 0

        init {
            this.context = context.applicationContext
        }

        override fun onDownloadChanged(
            downloadManager: DownloadManager, download: Download, finalException: java.lang.Exception?
        ) {
            val notification: Notification =
//                if (download.state == Download.STATE_COMPLETED) {
//                notificationHelper.buildDownloadCompletedNotification(
//                    context,
//                    R.drawable.ic_baseline_downloading_24,
//                    null,
//                    Util.fromUtf8Bytes(download.request.data)
//                )
//            } else
                if (download.state == Download.STATE_FAILED) {
                    failedNumber += 1
                    notificationHelper.buildDownloadFailedNotification(
                        context,
                        R.drawable.ic_baseline_downloading_24,
                        null,
                        "$failedNumber download failed."
                    )
                } else {
                    return
                }
            NotificationUtil.setNotification(context, nextNotificationId, notification)
        }
    }

}