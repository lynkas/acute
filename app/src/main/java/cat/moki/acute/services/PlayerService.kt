package cat.moki.acute.services

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.media3.common.C.WAKE_MODE_NETWORK
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.ContentMetadata.KEY_CONTENT_LENGTH
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import cat.moki.acute.AcuteApplication
import cat.moki.acute.Home
import cat.moki.acute.models.cacheInfo
import cat.moki.acute.models.cacheUrl
import cat.moki.acute.models.isLocal
import cat.moki.acute.models.rawUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext
import kotlin.math.log


@UnstableApi
class PlayerService : MediaLibraryService(), CoroutineScope {
    private var job: Job = Job()
    private val TAG = "PlayerService"
    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this)
            .setWakeMode(WAKE_MODE_NETWORK)
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
        })

    }

    private fun playlistCacheAdaptor() {
        Log.d(TAG, "playlistCacheAdaptor: player.mediaItemCount: ${player.mediaItemCount}")
        for (i in 0 until player.mediaItemCount) {
            player.replaceMediaItem(i, mediaItemUriSetting(player.getMediaItemAt(i)))

        }
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