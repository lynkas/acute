package cat.moki.acute.services

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.media3.common.C.WAKE_MODE_LOCAL
import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.Assertions
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util.isBitmapFactorySupportedMimeType
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import cat.moki.acute.AcuteApplication
import cat.moki.acute.Home
import cat.moki.acute.R
import cat.moki.acute.utils.frescoImageDownload
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@UnstableApi
class PlayerService : MediaLibraryService(), CoroutineScope {
    private var job: Job = Job()
    private val TAG = "PlayerService"
    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setWakeMode(WAKE_MODE_LOCAL)
            .setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(cacheDataSource(this))
            )
            .build()
    }
    private val mediaLibrarySession: MediaLibrarySession by lazy {
        MediaLibrarySession.Builder(this, player, callback)
            .setSessionActivity(
                PendingIntent.getActivity(
                    /* context = */ this,
                    /* requestCode = */ 0,
                    /* intent = */ Intent(this, Home::class.java),
                    /* flags = */ PendingIntent.FLAG_IMMUTABLE.or(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                )
            )
            .setBitmapLoader(object : BitmapLoader {
                override fun supportsMimeType(mimeType: String): Boolean {
                    return isBitmapFactorySupportedMimeType(mimeType)
                }

                override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
                    Log.d(TAG, "decodeBitmap: ${data.size}")
                    return future(Dispatchers.IO) {
                        val bitmap = BitmapFactory.decodeByteArray(data,  /* offset= */0, data.size)
                        Assertions.checkArgument(bitmap != null, "Could not decode image data")
                        bitmap
                    }
                }

                override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
                    Log.d(TAG, "loadBitmap: uri $uri")
                    Log.d(TAG, "loadBitmap: use cache only  ${!AcuteApplication.useOnlineSource && !(uri.scheme?.startsWith("file") ?: false)}")
                    return future(Dispatchers.IO) {
                        try {
                            frescoImageDownload(uri)
                        } catch (e: Exception) {
                            Log.w(TAG, "loadBitmap: ", e)
                            // throw exception will break the whole notification thing
                            return@future Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)
                        }
                    }
                }

            })
            .build()
    }
    private var callback: MediaLibrarySession.Callback = MediaLibrarySessionCallback(this, ::playlistCacheAdaptor)

    //    private lateinit var wakeLock: WakeLock
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

//    private fun setWakeLock(): WakeLock {
//        return (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
//            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cat.moki.acute::wakeLock").apply {
//                acquire()
//            }
//        }
//    }

    override fun onCreate() {
        super.onCreate()
        registerInternetChange(this, onChange = { internet, metered ->
            launch(Dispatchers.Main) {
                Log.d(TAG, "onCreate: internet change ${internet} ${metered}")
                playlistCacheAdaptor()
            }
        })
        setListener(MediaSessionServiceListener())

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.w(TAG, "onPlayerError: ", error)
                when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                        player.seekToNext()
                        player.prepare()
                        player.play()
                    }

                    else -> {
                        Log.w(TAG, "onPlayerError: not caught playback error", error)
                    }
                }
                super.onPlayerError(error)
            }

            override fun onTracksChanged(tracks: Tracks) {
                if (player.currentMediaItem?.mediaMetadata?.isPlayable != true) {
                    player.seekToNext()
                    player.prepare()
                    player.play()
                }
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                Log.d(TAG, "onMediaMetadataChanged: ${mediaMetadata.artworkUri}")
                super.onMediaMetadataChanged(mediaMetadata)
            }
        })

    }

    private fun playlistCacheAdaptor() {
        Log.d(TAG, "playlistCacheAdaptor: player.mediaItemCount: ${player.mediaItemCount}")
        for (i in 0 until player.mediaItemCount) {
            player.replaceMediaItem(i, mediaItemUriSetting(player.getMediaItemAt(i)))

        }
    }

    private inner class MediaSessionServiceListener : Listener {

    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        mediaLibrarySession.run {
            player.release()
            release()
            mediaLibrarySession.release()
        }
        job.cancel()
        super.onDestroy()
    }

}