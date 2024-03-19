package cat.moki.acute.viewModels

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import cat.moki.acute.AcuteApplication
import cat.moki.acute.components.library.ViewBy
import cat.moki.acute.models.MediaId
import cat.moki.acute.models.playlist
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.guava.await

class LibraryViewModel : ViewModel() {

    var loaded = mutableStateOf(false)
    var serverId = mutableStateOf(AcuteApplication.application.defaultServerId!!)
    val albumList = mutableStateListOf<MediaItem>()
    val size = mutableIntStateOf(20)
    val viewBy: MutableState<ViewBy> = mutableStateOf(ViewBy.list)
    val searchResultTime = mutableLongStateOf(System.currentTimeMillis())
    val detailedList = mutableStateListOf<MediaItem>()
    val detail = mutableStateOf<MediaItem?>(null)
    val cacheDetail = mutableStateOf<MediaItem?>(null)

//    val serverIdPlaylists = mutableStateMapOf<String, List<MediaItem>?>()
//    val serverIdPlaylistSongs = mutableStateMapOf<String, Map<String, List<MediaItem>>?>()

    val inited = mutableStateOf(false)
    lateinit var browser: MutableState<MediaBrowser>

    @Volatile
    private var loadingLock = false
    fun setBrowser(browser: MediaBrowser) {
        inited.value = true
        this.browser = mutableStateOf(browser)
    }

    val loading = mutableStateOf(false)
    private val TAG = "LibraryViewModel"
    val lastLoad = mutableIntStateOf(0)

//    private var root: MediaItem? = null
//    private suspend fun getRoot(browser: MediaBrowser): MediaItem {
//        root ?: run {
//            root = browser.getLibraryRoot(null).await().value
//        }
//        return root!!
//    }

    fun resetDetails() {
        detailedList.clear()
        detail.value = null
    }

    private suspend fun getDetailList(function: suspend (browser: MediaBrowser) -> ListenableFuture<LibraryResult<ImmutableList<MediaItem>>>): List<MediaItem> {
        resetDetails()
        val result = function(browser.value).await()
        result.value?.let {
            detailedList.addAll(it)
        } ?: run {
            Log.e("TAG", "getDetailList: request error")
        }
        return detailedList
    }

    private suspend fun getDetail(function: suspend (browser: MediaBrowser) -> ListenableFuture<LibraryResult<MediaItem>>): MediaItem? {
        resetDetails()
        val result = function(browser.value).await()
        result.value?.let {
            detail.value = result.value
        } ?: run {
            Log.e("TAG", "getDetail: request error")
        }

        return detail.value
    }


    suspend fun getAlbumSongList(mediaId: MediaId, force: Boolean = false): List<MediaItem> {
        return getDetailList {
            it.getChildren(mediaId.toString(), 0, Int.MAX_VALUE, null)
        }
    }


//    suspend fun getPlaylist(serverId: String, force: Boolean = false) {
//        val mediaId = MediaId(serverId, MediaMetadata.MEDIA_TYPE_PLAYLIST, "root")
//        val result = browser.value.getChildren(mediaId.toString(), 0, Int.MAX_VALUE, null).await()
//        playlist[serverId] = mutableStateListOf()
//        if (result.value != null)
//            playlist[serverId]?.addAll(result.value!!)
//    }


    suspend fun getPlaylists(serverId: String, force: Boolean = false): Map<MediaItem, List<MediaItem>> {
        val mediaId = MediaId(serverId, MediaMetadata.MEDIA_TYPE_PLAYLIST, MediaId.Root)
        Log.d(TAG, "getPlaylistDetail: start")
        val playlistData = browser.value.getChildren(mediaId.toString(), 0, Int.MAX_VALUE, null).await()
        if (playlistData.resultCode != LibraryResult.RESULT_SUCCESS) {
            Log.w(TAG, "getPlaylists: request failed${playlistData}")
        } else {
//            serverIdPlaylists[serverId] = playlistData.value
        }
        val map = mutableMapOf<MediaItem, List<MediaItem>>()
        playlistData.value?.let {
            map.putAll(it.associate { it ->
                Pair(it, it.playlist.entry.map { it.mediaItemWithoutUri })
            })
        }
//        serverIdPlaylistSongs[serverId] = map
        Log.d(TAG, "getPlaylistDetail: end")
        Log.d(TAG, "getPlaylistDetail: ${map.keys.size}")
        return map

    }

//    suspend fun getPlaylists(serverId: String, id: String, force: Boolean = false) {
//        return getPlaylists(serverId)
//    }

    suspend fun getPlaylistSongList(mediaId: MediaId, force: Boolean = false): List<MediaItem> {
        Log.d(TAG, "getPlaylistSongList: start")
        return getDetailList {
            it.getChildren(mediaId.toString(), 0, Int.MAX_VALUE, null)
        }
    }

    suspend fun getAlbumDetail(mediaId: MediaId, force: Boolean = false): MediaItem? {
        return getDetail {
            it.getItem(mediaId.toString())
        }
    }

    suspend fun getSong(mediaId: MediaId, force: Boolean = false) {
        getDetail {
            it.getItem(mediaId.toString())
        }
    }

    suspend fun getNextAlbums(): List<MediaItem>? {
        if (loadingLock) return null;
        loadingLock = true;
        if (loaded.value) {
            return emptyList()
        }
        loading.value = true
//        val root = getRoot(browser = browser.value)
        val page = albumList.size / size.intValue
        val remain = albumList.size % size.intValue
        Log.d(TAG, "getNextAlbums: page ${page} remain ${remain}")
        val mediaId = MediaId(serverId.value, MediaMetadata.MEDIA_TYPE_ALBUM, MediaId.Root)

        val result = browser.value.getChildren(mediaId.toString(), page, size.intValue, null).await()
        if (result.resultCode != LibraryResult.RESULT_SUCCESS) {
            Log.e("TAG", "getAlbums: request error")
        }
        Log.d("reachBottom", "Library: query succeeded")
        if (result.value == null) {
            Log.w("Library UI", "Library: resultCode ${result.resultCode} but result.value is null")
            loading.value = false
            loadingLock = false
            return null
        }
        Log.d("reachBottom", "Library:response length ${result.value!!.size}")
        if (result.value!!.size == 0) {
            loaded.value = true
            loadingLock = false
            return null
        }
        val append = result.value!!.slice(remain until size.intValue.coerceAtMost(result.value!!.size))
        append.forEach {
            albumList.add(it)
        }
        if (result.value!!.size < size.intValue) {
            loaded.value = true
        }
        Log.d("reachBottom", "Library:albumList length ${albumList.size}")
        loadingLock = false
        loading.value = false
        return append
    }

    fun lockReset() {
        loadingLock = false
    }
}