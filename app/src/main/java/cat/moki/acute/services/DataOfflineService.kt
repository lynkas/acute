package cat.moki.acute.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import cat.moki.acute.client.Client
import cat.moki.acute.models.Album
import cat.moki.acute.models.ServerCacheStatus
import cat.moki.acute.services.aidl.ICacheServer
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


typealias DownloadTask = suspend () -> Unit

class DataOfflineService : Service(), CoroutineScope {
    val TAG = "DataOfflineService"
    val taskStatus = mutableMapOf<String, ServerCacheStatus>()
    val tasks = mutableMapOf<String, Job>()

    private val binder = object : ICacheServer.Stub() {
        override fun cacheOperation(operation: String, serverId: String) {
            when (operation) {
                "start" -> tasks[serverId] = launch(Dispatchers.IO) {
                    runServerTask(serverId)
                }

                "cancel" -> tasks[serverId]?.cancel()

            }
        }

        override fun cacheStatus(): Map<String, ServerCacheStatus> {
            return taskStatus
        }

    }

    fun createChannel() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            "NOTIFICATION_CHANNEL_ID",
            "NOTIFICATION_CHANNEL_NAME",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

    }

    fun startNotification() {
        createChannel()
        startForeground(3, NotificationCompat.Builder(this, "NOTIFICATION_CHANNEL_ID").build())
    }

    fun endNotification() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    @OptIn(UnstableApi::class)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {


        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            "NOTIFICATION_CHANNEL_ID",
            "NOTIFICATION_CHANNEL_NAME",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")

    suspend fun runServerTask(id: String) {
        startNotification()
        taskStatus[id] = ServerCacheStatus(0, 1, "running")
        val albumList = mutableListOf<Album>()
        var failedCount = 3
        var offset = 0
        val size = 100
        while (true) {
            val req = Client.store(this, id, Client.Type.Online).getAlbumList(size = size, offset = offset)
            if (req.isEmpty()) break
            albumList.addAll(req)
            offset += req.size
            taskStatus[id]?.let { taskStatus[id] = it.copy(totalTasks = albumList.size) }
        }
        albumList.forEach {
            val album = Client.store(this, id, Client.Type.Online).getAlbumDetail(it.id)
            Glide.with(this).load(album.id).submit()
            taskStatus[id]?.let { taskStatus[id] = it.copy(finishedTask = it.finishedTask + 1) }
        }

        Client.store(this, id, Client.Type.Online).getPlaylists()

        taskStatus[id]?.let { taskStatus[id] = it.copy(status = "finish") }
        if (taskStatus.filter { (id, status) -> status.status == "running" }.isEmpty())
            endNotification()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder;
    }
}