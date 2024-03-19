package cat.moki.acute.components.album

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import cat.moki.acute.models.Album

class AlbumDetailData : ViewModel() {
    var album = mutableStateOf<Album?>(null, neverEqualPolicy())
}
