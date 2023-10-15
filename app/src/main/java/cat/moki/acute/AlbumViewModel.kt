package cat.moki.acute

import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import cat.moki.acute.models.Album
import cat.moki.acute.models.Song
import cat.moki.acute.client.Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
class AlbumDetailData constructor(private val _album: Album) : Parcelable {

    private var _songList = mutableStateListOf<Song>()
    private var loaded = false
    private var _sameArtist: MutableState<Boolean> = mutableStateOf(true)
    val songs: List<Song>
        get() = _songList

    val sameArtist: Boolean
        get() = _sameArtist.value

    val album: Album
        get() = _album

    fun getSongList(context: Context) {
        if (loaded) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Client.store(context).getAlbumDetail(album.id).song?.let { songList ->
                    _songList.addAll(songList)
                    _sameArtist.value =
                        songList.all { it.artist == _album.artist }
                    loaded = true
                }
            } catch (e: Exception) {
                Log.e("get song list", e.toString())
            }
        }
    }
}