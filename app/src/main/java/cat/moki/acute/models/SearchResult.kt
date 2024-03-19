package cat.moki.acute.models

import android.util.Log
import androidx.media3.common.MediaItem

data class SearchResult2(
    val artist: List<Artist>,
    val album: List<Album>,
    val song: List<Song>
)

data class SearchResult3(
    val artist: List<Artist>,
    val album: List<Album>,
    val song: List<Song>
) {
    fun toMediaItemList(): List<MediaItem> {
        Log.d(TAG, "toMediaItemList: $this")
        return listOf(
            *song.map { it.mediaItem }.toTypedArray(),
            *album.map { it.mediaItem }.toTypedArray(),
            *artist.map { it.mediaItem }.toTypedArray()
        )
    }
}

