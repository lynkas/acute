package cat.moki.acute.services

import android.util.Log
import androidx.media3.common.MediaItem
import cat.moki.acute.AcuteApplication
import cat.moki.acute.models.cacheInfo
import cat.moki.acute.models.rawUrl

fun mediaItemUriSetting(mediaItem: MediaItem): MediaItem {
    val TAG = "mediaItemUriSetting"
    Log.d(TAG, "mediaItemUriSetting: ${AcuteApplication.application.useOnlineSource} ${AcuteApplication.fullyCached(mediaItem.cacheInfo)}")
    val playable = AcuteApplication.application.useOnlineSource || AcuteApplication.fullyCached(mediaItem.cacheInfo)
    return mediaItem.buildUpon().apply {
        Log.d(TAG, "mediaItemUriSetting: AcuteApplication.application.useOnlineSource ${AcuteApplication.application.useOnlineSource}")
        Log.d(TAG, "mediaItemUriSetting: it.rawUrl ${mediaItem.rawUrl}")
        setUri(if (playable) mediaItem.rawUrl else "")
        setMediaMetadata(
            mediaItem.mediaMetadata.buildUpon().apply {
                setIsPlayable(playable)
            }.build()
        )
    }.build()
}