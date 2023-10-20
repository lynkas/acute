package cat.moki.acute

import android.util.Log
import androidx.media3.common.MediaItem
import cat.moki.acute.models.Album
import cat.moki.acute.models.Song

fun Int.formatSecond(): String {
    val h: Int = this / 3600
    val m: Int = this % 3600 / 60
    val s: Int = this % 60
    val result: String = if (h > 0) {
        "${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    } else {
        "${m}:${s.toString().padStart(2, '0')}"
    }
    return result
}

fun String.extractComplexMediaId(): Pair<String, String> {
    val values = this.split("/")
    return Pair(values[0], values[1])
}

//fun String.isAlbum(): Boolean {
//    return this.startsWith("al-")
//}
//
//fun String.isSong(): Boolean {
//    return this.startsWith("tr-")
//}

val MediaItem.album: Album
    get() = this.mediaMetadata.extras?.run {
        classLoader = Album::class.java.classLoader
        val _album: Album = getParcelable("album")!!
        Log.d("MediaItem.album: Album", "album id ${_album.id} songs ${_album.song?.size}")
        _album
    }!!

val MediaItem.songs: List<MediaItem>?
    get() = this.album.songMediaItemList

val MediaItem.song: Song
    get() = this.mediaMetadata.extras?.run {
        classLoader = Song::class.java.classLoader
        getParcelable("song")
    }!!