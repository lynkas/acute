package cat.moki.acute

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import cat.moki.acute.models.Album
import cat.moki.acute.client.Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class LibraryData(
    var _loaded: MutableState<Boolean> = mutableStateOf(false),
    val _albumList: MutableList<Album> = ArrayList<Album>().toMutableStateList(),
    val _albumMap: MutableMap<String, Album> = HashMap<String, Album>().toMutableMap(),
    val viewBy: MutableState<ViewBy> = mutableStateOf(ViewBy.list),
)

class LibraryViewModel : ViewModel() {
    @Volatile
    private var onRequestLock = false
    private val TAG = "LibraryViewModel"
    private var _data = LibraryData()
    val albumList: List<Album>
        get() = _data._albumList

    val loaded: Boolean
        get() = _data._loaded.value

    var viewBy: ViewBy
        get() = _data.viewBy.value
        set(value) {
            _data.viewBy.value = value
        }

    fun getAlbumByID(id: String): Album? {
        return _data._albumMap[id]
    }

    fun get(context: Context) {
        if (onRequestLock) {
            return
        }
        onRequestLock = true
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Make request")
            try {
                val list = Client.store(context = context).getAlbumList(offset = albumList.size)
                Log.d(TAG, "request succeeded")
                Log.d(TAG, "list size ${_data._albumList.size}")
                _data._loaded.value = list.isEmpty()
                _data._albumList.addAll(list)
//                AppDatabase.getInstance(context = context).album()
//                    .insertAll(*list.toTypedArray())
//                Log.d("db", AppDatabase.getInstance(context).album().getAll().size.toString())
                val tmp = HashMap<String, Album>()
                _data._albumList.forEach {
                    tmp[it.id] = it
                }
                _data._albumMap.putAll(tmp)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            } finally {
                onRequestLock = false
            }
        }
    }

    fun toggleView() {
        _data.viewBy.value = if (_data.viewBy.value == ViewBy.list) ViewBy.grid else ViewBy.list
    }

    fun reset() {
        _data = LibraryData()
    }


}