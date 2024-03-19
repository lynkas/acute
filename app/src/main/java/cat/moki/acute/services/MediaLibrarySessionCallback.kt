package cat.moki.acute.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.LibraryResult.RESULT_ERROR_BAD_VALUE
import androidx.media3.session.LibraryResult.ofItemList
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import cat.moki.acute.AcuteApplication
import cat.moki.acute.Command
import cat.moki.acute.Const
import cat.moki.acute.client.Client
import cat.moki.acute.client.LocalClient
import cat.moki.acute.models.Playlist
import cat.moki.acute.models.SearchResult3
import cat.moki.acute.models.duration
import cat.moki.acute.models.rawUrl
import cat.moki.acute.models.song
import cat.moki.acute.models.songFilled
import cat.moki.acute.models.toMediaId
import cat.moki.acute.routes.Strings
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.guava.future
import java.util.Date
import kotlin.coroutines.CoroutineContext

@UnstableApi
class MediaLibrarySessionCallback(val context: Context, val updatePlaylistInternetCache: () -> Unit) : MediaLibrarySession.Callback, CoroutineScope {
    private val TAG = "MediaLibrarySession.Callback"
    private var job: Job = Job()
    private val searchResults = mutableMapOf<String, SearchResult3>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        Log.d(TAG, "onCustomCommand: ${customCommand}")
        when (customCommand.customAction) {
            Command.UpdateInternetStatus.value -> updatePlaylistInternetCache()
            else -> {}
        }
        return super.onCustomCommand(session, controller, customCommand, args)
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
        Log.d(TAG, "onAddMediaItems: ${mediaItems.size}")
        val mediaItems = mediaItems.map(::mediaItemUriSetting)
        return super.onAddMediaItems(mediaSession, controller, mediaItems)
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        Log.d("asdasd", "onSetMediaItems: ")
        return super.onSetMediaItems(
            mediaSession,
            controller,
            mediaItems,
            startIndex,
            startPositionMs
        )
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        Log.d(TAG, "onGetLibraryRoot: get root")
        return Futures.immediateFuture(
            LibraryResult.ofItem(
                MediaItem.Builder()
                    .setMediaId(Const.Root)
                    .setMediaMetadata(MediaMetadata.Builder().setIsBrowsable(true).setIsPlayable(false).build())
                    .build(), params
            )
        )
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        complexMediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val mediaId = complexMediaId.toMediaId()
        Log.d(TAG, "onGetItem: complexMediaId $complexMediaId")

        suspend fun item(): MediaItem {
            return when (mediaId.mediaType) {
                MediaMetadata.MEDIA_TYPE_MUSIC -> LocalClient(context, mediaId.serverId).getSongIn(listOf(mediaId.itemId)).first().mediaItem
                MediaMetadata.MEDIA_TYPE_ALBUM -> Client.store(context, mediaId.serverId).getAlbumDetail(mediaId.itemId).mediaItem
                MediaMetadata.MEDIA_TYPE_PLAYLIST -> Client.store(context, mediaId.serverId).getPlaylist(mediaId.itemId).mediaItem
                else -> {
                    Log.e(TAG, "onGetItem: path $complexMediaId isn't processed")
                    throw IllegalAccessException()
                }
            }
        }

        return future {
            try {
                LibraryResult.ofItem(item(), null)
            } catch (e: Exception) {
                LibraryResult.ofError(RESULT_ERROR_BAD_VALUE)
            }
        }

    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        complexMediaId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        suspend fun item(): List<MediaItem> {
            val mediaId = complexMediaId.toMediaId()
            return when (mediaId.mediaType) {
                MediaMetadata.MEDIA_TYPE_MUSIC -> Client.store(context, mediaId.serverId).getAlbumList(size = pageSize, offset = pageSize * page)
                    .map {
                        it.mediaItem
                    }

                MediaMetadata.MEDIA_TYPE_PLAYLIST -> if (mediaId.serverId == Strings.Local) {


                    val result = mutableMapOf<String, MutableList<MediaItem>>()
                    AcuteApplication.application.allCacheFileMediaItem(
                        AcuteApplication.application.allCacheFinishedFiles().filter { it.downloading == false })
                        .forEach { (file, mediaItem) ->
                            if (file.serverId !in result) {
                                result[file.serverId] = mutableListOf()
                            }
                            if (mediaItem != null) {
                                result[file.serverId]!!.add(mediaItem)
                            }
                        }
                    result.map { (id, list) ->
                        Playlist(id, id, "", "", "", list.size, Date(), (list.sumOf { it.duration } / 1000).toInt(),
                            entry = list.map { it.song }).mediaItem
                    }

                } else if (mediaId.isRoot) {
                    Client.store(context, mediaId.serverId).getPlaylists().map { it.mediaItem }
                } else {
                    Client.store(context, mediaId.serverId, type = Client.Type.Local)
                        .getPlaylist(id = mediaId.itemId).entry.map { it.mediaItemWithoutUri }
                }

                MediaMetadata.MEDIA_TYPE_ALBUM -> {
                    if (mediaId.isRoot) {
                        Client.store(context, mediaId.serverId)
                            .getAlbumList(size = pageSize, offset = page * pageSize).map { it.mediaItem }
                    } else {
                        Client.store(context, mediaId.serverId)
                            .getAlbumDetail(id = mediaId.itemId).song!!.map {
                                it.mediaItemWithoutUri
                            }
                    }
                }

                else -> {
                    Log.e(TAG, "onGetItem: path $complexMediaId isn't processed")
                    throw IllegalAccessException()
                }
            }
        }

        return future {
            try {
                LibraryResult.ofItemList(item(), params)
            } catch (e: Exception) {

                Log.e(TAG, "onGetChildren: ", e)
                LibraryResult.ofError(RESULT_ERROR_BAD_VALUE)
            }
        }
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<Void>> {
        val results = AcuteApplication.application.servers.map {
            async {
                searchResults[it.id] = Client.store(context, it.id).search3(query)
                Log.d(TAG, "onSearch: ${searchResults[it.id]}")
            }
        }

        return future {
            awaitAll(*results.toTypedArray())
            session.notifySearchResultChanged(browser, query, 0, params)
            LibraryResult.ofVoid()
        }


    }


    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        Log.d(TAG, "onGetSearchResult: ${searchResults.values.map { it.toMediaItemList() }.flatten()}")
        return Futures.immediateFuture(ofItemList(searchResults.values.map { it.toMediaItemList() }.flatten(), params))
    }

    override fun onMediaButtonEvent(session: MediaSession, controllerInfo: MediaSession.ControllerInfo, intent: Intent): Boolean {
        Log.d(TAG, "onMediaButtonEvent: ${intent}")
        return super.onMediaButtonEvent(session, controllerInfo, intent)
    }

}