package cat.moki.acute

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
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
    get() {
        val albumBundle = this.mediaMetadata.extras?.getBundle("album")
        return albumBundle?.getParcelable<Album>("album")!! as Album
    }

val MediaItem.songs: List<MediaItem>?
    get() = this.album.songMediaItemList

val MediaItem.song: Song
    get() = this.mediaMetadata.extras?.getParcelable<Song>("song")!!
