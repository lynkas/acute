package cat.moki.acute.models

import android.net.Uri
import android.os.Parcelable
import android.util.Log
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(primaryKeys = ["id", "server"])
@Parcelize
data class Artist(
    val id: String,
    var server: String = "",
    val name: String,
    @Ignore val artistImageUrl: String? = null
) : Parcelable {
    constructor(
        id: String,
        server: String = "",
        name: String
    ) : this(
        id,
        server,
        name,
        null
    )

    val mediaItem: MediaItem
        get() {
            return MediaItem.Builder().apply {
                setMediaId(id)
                setMediaMetadata(MediaMetadata.Builder().apply {
                    setIsPlayable(false)
                    setArtist(name)
                    setTitle(name)
                    setMediaType(MediaMetadata.MEDIA_TYPE_ARTIST)
                    setIsBrowsable(false)
                    try {
                        artistImageUrl?.let { setArtworkUri(Uri.parse(it)) }
                    } catch (e: Exception) {
                        Log.w(TAG, "artistImageUrl: ${id}, ${name}")
                    }
                    setExtras(bundleOf().apply {
                        putParcelable("artist", this)
                        putString("serverId", server)
                    })
                }.build())
            }.build()
        }


}

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg artist: Artist)

    @Query("SELECT * FROM artist where id=:id and server=:serverId")
    fun get(id: String, serverId: String): Artist

}
