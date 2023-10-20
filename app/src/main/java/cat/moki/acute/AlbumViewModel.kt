package cat.moki.acute

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import cat.moki.acute.models.Album

class AlbumDetailData : ViewModel() {
    var album = mutableStateOf<Album?>(null, neverEqualPolicy())
}
//
//@Parcelize
//class AlbumDetailData constructor(private val _album: Album) : Parcelable {
//
//    private var _songList = mutableStateListOf<Song>()
//    private var loaded = false
//    private var _sameArtist: MutableState<Boolean> = mutableStateOf(true)
//    val songs: List<Song>
//        get() = _songList
//
//    val sameArtist: Boolean
//        get() = _sameArtist.value
//
//    val album: Album
//        get() = _album
//
//    fun getSongList(context: Context) {
//        if (loaded) return
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                Client.store(context).getAlbumDetail(album.id).song?.let { songList ->
//                    _songList.addAll(songList)
//                    _sameArtist.value =
//                        songList.all { it.artist == _album.artist }
//                    loaded = true
//                }
//            } catch (e: Exception) {
//                Log.e("get song list", e.toString())
//            }
//        }
//    }
//}