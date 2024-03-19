package cat.moki.acute.models

import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.os.Parcelable
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_ALBUM
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_PLAYLIST
import androidx.media3.common.MediaMetadata.MediaType
import androidx.media3.common.MimeTypes
import androidx.room.*
import cat.moki.acute.Const
import cat.moki.acute.client.NetClient
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

data class Res(
    @SerializedName("subsonic-response")
    val subsonicResponse: SubsonicResponse
) {
    val ok: Boolean
        get() = subsonicResponse.status == "ok"
}

data class SubsonicResponse(
    val status: String,
    val version: String,
    val type: String,
    val albumList: AlbumList?,
    val albumList2: AlbumList2?,
    val album: Album?,
    val playlist: Playlist?,
    val playlists: Playlists?,
    val error: Error?,
    val scanStatus: ScanStatus?,
    val searchResult2: SearchResult2?,
    val searchResult3: SearchResult3?,
) {
    fun setServer(serverId: String) {
        albumList?.album?.forEach {
            it.server = serverId
            it.song?.forEach { it.server = serverId }
        }
        albumList2?.album?.forEach {
            it.server = serverId
            it.song?.forEach { it.server = serverId }
        }
        album?.let {
            it.server = serverId
            it.song?.forEach { it.server = serverId }
        }

        playlist?.let {
            it.server = serverId
            it.entry.forEach { it.server = serverId }
        }

        playlists?.playlist?.forEach {
            it.server = serverId
            it.entry.forEach { it.server = serverId }
        }

        searchResult2?.let {
            it.song.forEach { it.server = serverId }
            it.album.forEach {
                it.server = serverId
                it.song?.forEach { it.server = serverId }
            }
            it.artist.forEach { it.server = serverId }
        }

        searchResult3?.let {
            it.song.forEach { it.server = serverId }
            it.album.forEach {
                it.server = serverId
                it.song?.forEach { it.server = serverId }
            }
            it.artist.forEach { it.server = serverId }
        }


    }
}

data class Error(
    val code: Int?,
    val message: String?
)


data class AlbumList(
    val album: List<Album>
)

data class AlbumList2(
    val album: List<Album>
)


object Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Entity
@Parcelize
data class Playlists(
    val playlist: List<Playlist>
) : Parcelable


