package cat.moki.acute.models

import android.os.Bundle
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import cat.moki.acute.Const
import kotlinx.parcelize.Parcelize
import org.jetbrains.annotations.NotNull
import java.util.Date


@Entity(primaryKeys = ["id", "server"])
@Parcelize
data class Playlist(
    val id: String,
    var server: String = "",
    val name: String,
    val comment: String,
    val owner: String,
    val songCount: Int,
    val created: Date,
    val duration: Int,
    @Ignore val entry: List<Song>?
) : Parcelable {
    constructor(
        id: String,
        server: String,
        name: String,
        comment: String,
        owner: String,
        songCount: Int,
        created: Date,
        duration: Int,
    ) : this(
        id,
        server,
        name,
        comment,
        owner,
        songCount,
        created,
        duration,
        emptyList()
    )

//    fun setServer(serverId: String) {
//        server = serverId
//        entry.forEach { it.setServer(serverId) }
//    }

    val mediaItem: MediaItem
        get() = MediaItem.Builder().apply {
            setMediaId(id)
            setMediaMetadata(
                MediaMetadata.Builder()
                    .apply {
                        setMediaType(MediaMetadata.MEDIA_TYPE_PLAYLIST)
                        setTitle(name)
                        setIsBrowsable(true)
                        setIsPlayable(false)
                        setAlbumTitle(name)
                        val data = Bundle()
                        data.putParcelable(Const.Playlist, this@Playlist)
                        data.putLong(Const.Duration, duration * 1000L)
                        data.putInt(Const.Count, songCount)
                        data.putString(Const.Server, server)
                        setExtras(data)
                    }.build()
            )
        }.build()
}


@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg playlists: Playlist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(playlists: Playlist)

    @Delete
    fun delete(playlist: Playlist)

    @Query("DELETE FROM playlist where id=:id and server=:serverId")
    fun delete(id: String, serverId: String)

    @Query("SELECT * FROM playlist")
    fun getAll(): List<Playlist>


    @Query("SELECT * FROM playlist where server=:serverId")
    fun getAllFromServer(serverId: String): List<Playlist>

    @Query("SELECT * FROM playlist where id=:id and server=:serverId")
    fun get(id: String, serverId: String): Playlist

}
