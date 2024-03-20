package cat.moki.acute.models

import android.util.Log
import androidx.media3.common.MediaItem

data class SearchResult2(
    val artist: List<Artist>,
    val album: List<Album>,
    val song: List<Song>
)

data class SearchResult3(
    val artist: List<Artist>?,
    val album: List<Album>?,
    val song: List<Song>?
) {
    fun toMediaItemList(): List<MediaItem> {
        Log.d(TAG, "toMediaItemList: $this")
        return mutableListOf<MediaItem>().apply {
            song?.let { it.map { it.mediaItem }.toTypedArray() }?.also { addAll(it) }
            album?.let { it.map { it.mediaItem }.toTypedArray() }?.also { addAll(it) }
            artist?.let { it.map { it.mediaItem }.toTypedArray() }?.also { addAll(it) }
        }
    }
}

