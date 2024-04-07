package cat.moki.acute.models

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import cat.moki.acute.Const
import cat.moki.acute.client.NetClient
import kotlinx.parcelize.Parcelize
import java.util.Date


@Entity(primaryKeys = ["id", "server"])
@Parcelize
data class Album(
    val id: String,
    var server: String = "",
    val coverArt: String?,
    val artist: String?,
    val artistId: String?,
    val created: Date,
    val name: String,
    val songCount: Int,
    val duration: Int,
    val year: Int?,
    val genre: String?,
    val playCount: Long?,
    @Ignore var song: List<Song>? = null,
) : Parcelable {
    constructor(
        id: String,
        server: String,
        coverArt: String?,
        artist: String,
        artistId: String,
        created: Date,
        name: String,
        songCount: Int,
        duration: Int,
        year: Int?,
        genre: String?,
        playCount: Long?,
    ) : this(
        id,
        server,
        coverArt,
        artist,
        artistId,
        created,
        name,
        songCount,
        duration,
        year,
        genre,
        playCount,
        null
    )

//    fun setServer(serverId: String) {
//        server = serverId
//        song?.forEach { it.setServer(serverId) }
//    }

    val mediaItem: MediaItem
        get() = MediaItem.Builder().apply {
            setMediaId(id)
            setMediaMetadata(MediaMetadata.Builder().apply {
                setTitle(name)
                setArtist(artist)
                setAlbumTitle(name)
                setAlbumArtist(artist)
                setAlbumTitle(name)
                setIsPlayable(false)
                setIsBrowsable(true)
                setArtworkUri(Uri.parse(NetClient.link(server).getCoverArtUrl(id)))
                setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                setGenre(genre)
                val data = Bundle()
                data.putParcelable(Const.Album, this@Album)
                data.putLong(Const.Duration, duration * 1000L)
                data.putInt(Const.Count, songCount)
                data.putString(Const.Server, server)
                if (song == null || song?.size == 0) {
                    Log.w(TAG, "album mediaitem ${id} ${name}: song list is empty")
                } else {
                    Log.d(TAG, "album mediaitem ${id} ${name}: song list ${song?.size}")
                }
                val tracker = System.currentTimeMillis()
                data.putLong("tracker", tracker)
                Log.d(TAG, "album mediaitem: tracker ${tracker}")
                song?.let {
                    data.putParcelableArrayList(Const.Songs, ArrayList<Song>(it))
                    Log.d(TAG, "album mediaitem ${id} ${name}: songs put")
                }
                Log.d(TAG, "song?.toTypedArray() :${id} ")
                Log.d(TAG, "song?.toTypedArray() :${name} ")
                Log.d(TAG, "song?.toTypedArray() :${song?.toTypedArray()} ")
                setExtras(data)
            }.build())
        }.build()

}

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg album: Album)

    @Delete
    fun delete(album: Album)

    @Query("DELETE FROM album where id=:id and server=:serverId")
    fun delete(id: String, serverId: String)

    @Query("SELECT * FROM album")
    fun getAll(): List<Album>


    @Query("SELECT * FROM album where server=:serverId")
    fun getAllFromServer(serverId: String): List<Album>

    @Query("SELECT * FROM album where id=:id and server=:serverId")
    fun get(id: String, serverId: String): Album

    @Query("SELECT * FROM album where server=:serverId order by created desc limit :limit offset :offset")
    fun getAll(limit: Int, offset: Int, serverId: String): List<Album>

}

