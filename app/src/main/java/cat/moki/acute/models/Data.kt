package cat.moki.acute.models

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_ALBUM
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import androidx.media3.common.MediaMetadata.MediaType
import androidx.media3.common.MimeTypes
import androidx.room.*
import cat.moki.acute.client.NetClient
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList

data class Res(
    @SerializedName("subsonic-response")
    val subsonicResponse: SubsonicResponse
)

data class SubsonicResponse(
    val status: String,
    val version: String,
    val type: String,
    val albumList: AlbumList?,
    val albumList2: AlbumList2?,
    val album: Album?,
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
data class Album(
    @PrimaryKey val id: String,
    val coverArt: String?,
    val artist: String,
    val created: Date,
    val title: String?,
    val album: String?,
    val parent: String?,
    val isDir: Boolean?,
    val name: String,
    val songCount: Int,
    val duration: Int,
    val year: Int?,
    @Ignore var song: List<Song>? = null,
) : Parcelable {
    constructor(
        id: String,
        coverArt: String?,
        artist: String,
        created: Date,
        title: String?,
        album: String?,
        parent: String?,
        isDir: Boolean?,
        name: String,
        songCount: Int,
        duration: Int,
        year: Int?,
    ) : this(
        id,
        coverArt,
        artist,
        created,
        title,
        album,
        parent,
        isDir,
        name,
        songCount,
        duration,
        year,
        null
    )

    fun find(songId: String): Song? = this.song?.find { song -> songId == song.id }
    val albumMediaItem: MediaItem
        get() {
            Log.d("TAG", "ToMediaItem: albumMediaItem")
            return ToMediaItem(null, this)
        }
    val songMediaItemList: List<MediaItem>?
        get() = song?.map { s ->
            Log.d("TAG", "ToMediaItem: songMediaItemList")
            ToMediaItem(s.id, this)
        }

    val sameArtist: Boolean
        get() = song?.all { it.artist == this.artist } ?: false

    val realTitle: String?
        get() = if (name == "") title else name

}

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg album: Album)

    @Delete
    fun delete(album: Album)

    @Query("DELETE FROM album where id=:id")
    fun delete(id: String)

    @Query("SELECT * FROM album")
    fun getAll(): List<Album>

    @Query("SELECT * FROM album where id=:id")
    fun get(id: String): Album

    @Query("SELECT * FROM album limit :limit offset :offset")
    fun getAll(limit: Int, offset: Int): List<Album>

}


@Entity
@Parcelize
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
data class Song(
    @PrimaryKey val id: String,
    val album: String,
    val albumId: String,
    val artist: String,
    val artistId: String,
    val bitRate: Int,
    val contentType: String,
    val coverArt: String?,
    val created: Date,
    val duration: Int,
    val isDir: Boolean,
    val isVideo: Boolean,
    val parent: String,
    val path: String,
    val size: Int,
    val suffix: String,
    val title: String,
    val track: Int,
    val discNumber: Int,
    val type: String,
    val year: Int,
) : Parcelable


@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun ToMediaItem(songId: String?, album: Album): MediaItem = MediaItem.Builder().apply {
    var mediaId = songId
    if (mediaId == "" || mediaId == null) mediaId = album.id
    setMediaId(mediaId)
    val song = album.song?.find { it.id == songId }
    if (songId != null) {
        setUri(NetClient.getStreamUrl(songId))
        setMimeType(MimeTypes.getAudioMediaMimeType(song?.suffix))
    }
    val albumTitle = album.realTitle
    setMediaMetadata(
        MediaMetadata.Builder()
            .apply {
                setArtworkUri(Uri.parse(NetClient.getCoverArtUrl(album.id)))
                if (song != null) {
                    setArtist(song.artist)
                    setMediaType(MEDIA_TYPE_MUSIC)
                    setTitle(song.title)
                } else {
                    setArtist(album.artist)
                    setMediaType(MEDIA_TYPE_ALBUM)
                    setTitle(albumTitle)
                }
                setIsBrowsable(song == null)
                setIsPlayable(song != null)
                setAlbumTitle(albumTitle)
                setAlbumArtist(album.artist)
                val data = Bundle()
                Log.d("TAG", "ToMediaItem: ${album.song?.size}")
                data.putParcelable("album", album)
                data.putParcelable("song", song)
                data.putParcelableArrayList("songs", album.song?.let { ArrayList(it) })
                setExtras(data)
            }.build()

    )

}.build()

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg song: Song)

    @Delete
    fun delete(song: Song)

    @Query("DELETE FROM song where id=:id")
    fun delete(id: String)

    @Query("SELECT * FROM song")
    fun getAll(): List<Song>

    @Query("SELECT * FROM song where albumId=:id")
    fun getAlbum(id: String): List<Song>

    @Query("SELECT * FROM song where id=:id")
    fun get(id: String): Song

}

