package cat.moki.acute.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import cat.moki.acute.R
import cat.moki.acute.models.ServerCacheStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext


class DataOfflineService : Service(), CoroutineScope {
    val TAG = "DataOfflineService"
    val tasks = mutableMapOf<String, SyncJobQueue>()
    val notificationId = AtomicInteger(5)
    private var job: Job = Job()
    val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }

    inner class DataOfflineBinder(
    ) : Binder() {
        fun cacheOperation(operation: String, serverId: String) {
            createChannel()

            when (operation) {
                "start" -> {
                    if (!tasks.containsKey(serverId) || tasks[serverId]?.status()?.status != "running") {
                        val q = SyncJobQueue(this@DataOfflineService, serverId, this@DataOfflineService)
                        val id = notificationId.addAndGet(1)
                        tasks[serverId] = q
                        val notificationJob = launch(Dispatchers.IO) {
                            while (true) {
                                updateNotification(id, q.status())
                                delay(500)
                            }
                        }
                        launch(Dispatchers.IO) {
                            updateNotification(id, q.status())
                            q.run()
                            notificationJob.cancel()
                            tasks.remove(serverId)
                            removeNotification(id)
                            if (tasks.isEmpty()) {

                                endNotification()
                            }
                        }
                    }
                }

                "cancel" -> tasks[serverId]?.cancel()

            }
        }

        fun cacheStatus(): Map<String, ServerCacheStatus> {
            return tasks.mapValues { it.value.status() }
        }

        fun getService(): DataOfflineService {
            return this@DataOfflineService
        }

    }

    private val binder = DataOfflineBinder()

    fun createChannel() {
        val channel = NotificationChannel(
            "ServerMetaSync",
            "Server Metadata Sync",
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)

    }

    fun buildNotification(serverCacheStatus: ServerCacheStatus): Notification {
        return NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL_ID").apply {
            setContentTitle("Caching Meta Information")
            setContentText("${serverCacheStatus.finishedTask} / ${serverCacheStatus.totalTasks}")
            setSmallIcon(R.drawable.ic_baseline_downloading_24)
        }.build()
    }

    fun updateNotification(id: Int, serverCacheStatus: ServerCacheStatus) {
        notificationManager.notify(id, buildNotification(serverCacheStatus))
    }

    fun removeNotification(id: Int) {
        notificationManager.cancel(id)

    }

    fun endNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    @OptIn(UnstableApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return super.onStartCommand(intent, flags, startId)
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onBind(intent: Intent?): IBinder {
        return binder;
    }
}