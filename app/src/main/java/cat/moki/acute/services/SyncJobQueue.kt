package cat.moki.acute.services

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import cat.moki.acute.client.Client
import cat.moki.acute.client.NetClient
import cat.moki.acute.models.ServerCacheStatus
import cat.moki.acute.utils.frescoImageDownload
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "SyncJobQueue"


sealed class TaskInfo {
    data object Finish : TaskInfo()
    data class AlbumList(val offset: Int, val size: Int) : TaskInfo()
    data class AlbumDetail(val albumId: String) : TaskInfo()
    data class AlbumCover(val albumId: String) : TaskInfo()
    data object Playlist : TaskInfo()
}

class SyncJobQueue(val context: Context, val serverId: String, private val scope: CoroutineScope, private val concurrentNumber: Int = 5) {
    private val queue = Channel<TaskInfo>(Channel.UNLIMITED)
    private var finishedCount = AtomicInteger()
    private var totalCount = AtomicInteger()
    private var sendFinished = false
    private var tasks = mutableListOf<Job>()
    private val client = Client.store(context, serverId, Client.Type.Online)

    suspend fun run() {
        submit(TaskInfo.AlbumList(0, 100))
        submit(TaskInfo.Playlist)
        for (i in 0 until concurrentNumber) {
            tasks.add(scope.launch(Dispatchers.IO) {
                while (true) {
                    val task = queue.receive()
                    if (task is TaskInfo.Finish) return@launch
                    runner(task)
                    finishedCount.addAndGet(1)
                }
            })

        }
        tasks.forEach { it.join() }
        Log.d(TAG, "run: done")
    }

    private suspend fun cacheAlbumCover(id: String) {
        val uri = NetClient.link(serverId).getCoverArtUrl(id).toUri()
        val imageRequest = ImageRequestBuilder
            .newBuilderWithSource(uri)
            .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
            .build()
        frescoImageDownload(uri = uri, imageRequest)

    }


    private suspend fun runner(task: TaskInfo) {
        when (task) {
            is TaskInfo.Finish -> delay(1000)
            is TaskInfo.AlbumList -> cacheAlbumList(task.offset, task.size)
            is TaskInfo.AlbumCover -> cacheAlbumCover(task.albumId)
            is TaskInfo.AlbumDetail -> cacheAlbumDetail(task.albumId)
            is TaskInfo.Playlist -> cachePlaylists()
        }
    }

    private suspend fun cachePlaylists() {
        client.getPlaylists()
        Log.d(TAG, "runServerTask: $serverId download playlist")

    }

    private suspend fun cacheAlbumDetail(id: String) {
        client.getAlbumDetail(id)
        Log.d(TAG, "runServerTask: $serverId download album detail ${id}")
    }

    private suspend fun cacheAlbumList(offset: Int, size: Int) {
        val albumList = client.getAlbumList(size = size, offset = offset)
        if (albumList.isEmpty()) {
            setFinishSend()
            return
        }
        Log.d(TAG, "runServerTask: $serverId download album amount offset $offset size $size ${albumList.size}")
        submit(TaskInfo.AlbumList(offset + size, size))
        albumList.forEach { album ->
            submit(TaskInfo.AlbumDetail(album.id))
            album.coverArt?.let { submit(TaskInfo.AlbumCover(it)) }
        }
    }

    private fun submit(task: TaskInfo) {
        if (sendFinished) throw IllegalStateException("already send finished, not allowed to append more tasks")
        queue.trySend(task)
        if (task !is TaskInfo.Finish) totalCount.addAndGet(1)
    }

    private fun setFinishSend() {
        sendFinished = true
        for (i in 0 until concurrentNumber)
            queue.trySend(TaskInfo.Finish)
    }

    fun status(): ServerCacheStatus {
        return ServerCacheStatus(finishedCount.get(), totalCount.get(), "running")
    }

    fun cancel() {
        queue.cancel()
        scope.cancel()
    }
}