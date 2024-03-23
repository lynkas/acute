package cat.moki.acute.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
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


    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")

    suspend fun runServerTask(id: String) {
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