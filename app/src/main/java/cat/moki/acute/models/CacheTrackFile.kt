package cat.moki.acute.models

import android.util.Log
import cat.moki.acute.utils.gson
import com.google.gson.Gson
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class CacheTrackFile(
    val serverId: String,
    val trackId: String,
    val bitrate: Int = 0,
    val format: String? = null,
    val downloading: Boolean? = false
) : Comparable<CacheTrackFile?> {
    companion object {

        private val regex = Regex("([^/]+)/([^/]+)?/(\\w*):(.+?)(\\.download)?\$")
        fun fromPath(s: String): CacheTrackFile {
            Log.d(TAG, "fromPath: ${s}")
            val result = regex.find(s)!!.groupValues
            return CacheTrackFile(
                result[1],
                result[2],
                result[3].toInt(),
                if (result[4] == "null") null else result[4],
                result.getOrNull(5) == ".download"
            )
        }

        fun fromPathOrNull(s: String): CacheTrackFile? {
            return try {
                fromPath(s)
            } catch (e: Exception) {
                Log.w(TAG, "fromPathOrNull: ", e)
                null
            }
        }
    }

    val pathWithoutDownload
        get() = "${serverId}/${trackId}/${bitrate}:${format}"

    val pathWithDownload
        get() = "${serverId}/${trackId}/${bitrate}:${format}" + if (downloading == true) ".download" else ""


    override fun compareTo(other: CacheTrackFile?): Int {
        if (other == null) return 1
        var a: Int = serverId.compareTo(other.serverId)
        if (a != 0) return a
        a = trackId.compareTo(other.trackId)
        if (a != 0) return a
        a = bitrate.compareTo(other.bitrate)
        if (a != 0) return a
        if (format == null) return 0
        if (other.format == null) return 1
        return format.compareTo(other.format)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CacheTrackFile) return false
        return serverId == other.serverId && trackId == other.trackId && bitrate == other.bitrate && format == other.format && downloading == other.downloading
    }
}
