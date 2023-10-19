package cat.moki.acute

import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import cat.moki.acute.client.Client
import cat.moki.acute.models.ToMediaItem
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.guava.future
import kotlin.coroutines.CoroutineContext


class PlayerService : MediaLibraryService(), CoroutineScope {
    private var job: Job = Job()
    private lateinit var player: ExoPlayer
    private var mediaLibrarySession: MediaLibrarySession? = null
    private var callback: MediaLibrarySession.Callback =
        @UnstableApi object : MediaLibrarySession.Callback {
            private val TAG = "MediaLibrarySession.Callback"
            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                return super.onCustomCommand(session, controller, customCommand, args)
            }


            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {
                Log.d(TAG, "onAddMediaItems: $mediaItems")
                return super.onAddMediaItems(mediaSession, controller, mediaItems)
            }

            override fun onSetMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>,
                startIndex: Int,
                startPositionMs: Long
            ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
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
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<MediaItem>> {
                Log.d(TAG, "onGetLibraryRoot: get root")
                return Futures.immediateFuture(
                    LibraryResult.ofItem(
                        MediaItem.Builder()
                            .setMediaId("root")
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
                val (albumId, songId) = complexMediaId.extractComplexMediaId()
                Log.d(TAG, "onGetItem: $albumId, $songId")
                return future {
                    val album = Client.store(this@PlayerService).getAlbumDetail(albumId)
                    val song = album.find(songId)
                    LibraryResult.ofItem(ToMediaItem(song?.id, album), null)
                }
            }

            override fun onGetChildren(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                parentId: String,
                page: Int,
                pageSize: Int,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
                Log.d(TAG, "onGetLibraryRoot: get root")
                return future {
                    if (parentId == "root") {
                        val albums = Client.store(this@PlayerService).getAlbumList(size = pageSize, offset = pageSize * page)
                        Log.d(TAG, "onGetChildren: children length: ${albums.size}")
                        return@future LibraryResult.ofItemList(albums.map { it.albumMediaItem }, params)
                    } else {
                        val (albumId, _) = parentId.extractComplexMediaId()
                        val album = Client.store(this@PlayerService).getAlbumDetail(albumId)
                        val mediaItems: List<MediaItem> = album.songMediaItemList ?: emptyList()
                        return@future LibraryResult.ofItemList(mediaItems, params)
                    }

                }
            }

            override fun onSearch(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                query: String,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<Void>> {
                return super.onSearch(session, browser, query, params)
            }

            override fun onGetSearchResult(
                session: MediaLibrarySession,
                browser: MediaSession.ControllerInfo,
                query: String,
                page: Int,
                pageSize: Int,
                params: LibraryParams?
            ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
                return super.onGetSearchResult(session, browser, query, page, pageSize, params)
            }
        }
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback).build()
    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        job.cancel()
        super.onDestroy()
    }

}