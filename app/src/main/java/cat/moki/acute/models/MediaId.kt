package cat.moki.acute.models

import android.util.Log
import androidx.media3.common.MediaMetadata.MediaType
import cat.moki.acute.utils.gson
import com.google.gson.Gson
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class MediaId(
    val serverId: String,
    val mediaType: @MediaType Int,
    val itemId: String
) {

    companion object {
        public val Root = "root"
        fun from(wholeId: String): MediaId {
            Log.d("MediaId", "from: ${wholeId}")
            return gson.fromJson(wholeId, MediaId::class.java)
        }
    }

    override fun toString(): String {
        return gson.toJson(this)
    }

    val isRoot
        get() = itemId == Root || itemId.isEmpty()

    @OptIn(ExperimentalEncodingApi::class)
    val base64: String
        get() = Base64.encode(toString().toByteArray()).toString()

    override fun equals(other: Any?): Boolean {
        if (other !is MediaId) return false
        return serverId == other.serverId && mediaType == other.mediaType && itemId == other.itemId
    }

}

fun String.toMediaId(): MediaId {
    Log.d("String.toMediaId()", "toMediaId: ${this}")
    return MediaId.from(this)
}