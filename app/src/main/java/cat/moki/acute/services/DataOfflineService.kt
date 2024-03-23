package cat.moki.acute.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.NotificationUtil
import androidx.media3.common.util.NotificationUtil.createNotificationChannel
import androidx.media3.common.util.UnstableApi
import cat.moki.acute.R
import cat.moki.acute.client.Client
import cat.moki.acute.models.Album
import cat.moki.acute.models.ServerCacheStatus
import cat.moki.acute.services.aidl.ICacheServer
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


typealias DownloadTask = suspend () -> Unit

class DataOfflineService : Service(), CoroutineScope {
    val TAG = "DataOfflineService"
    val taskStatus = mapOf<String, ServerCacheStatus>()

    private val binder = object : ICacheServer.Stub() {
        override fun cacheOperation(operation: String, serverId: String) {
            when (operation) {
                "start" -> launch(Dispatchers.IO) {
                    runServerTask(serverId)

                }
            }
        }

        override fun cacheStatus(): MutableMap<String, ServerCacheStatus> {
            TODO("Not yet implemented")
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
        val albumList = mutableListOf<Album>()
        var failedCount = 3
        var offset = 0
        val size = 100
        while (true) {
            val req = Client.store(this, id, Client.Type.Online).getAlbumList(size = size, offset = offset)
            if (req.isEmpty()) break
            albumList.addAll(req)
            offset += req.size
        }
        albumList.forEach {
            val album = Client.store(this, id, Client.Type.Online).getAlbumDetail(it.id)
            Glide.with(this).load(album.id).submit()
        }

        Client.store(this, id, Client.Type.Online).getPlaylists()


    }

    override fun onBind(intent: Intent?): IBinder {
        return binder;
    }
}