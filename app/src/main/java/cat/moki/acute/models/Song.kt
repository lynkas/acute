package cat.moki.acute.models

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_ALBUM
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cat.moki.acute.Const
import cat.moki.acute.Const.Companion.AlbumLocalMediaId
import cat.moki.acute.Const.Companion.CacheInfo
import cat.moki.acute.Const.Companion.CacheUrl
import cat.moki.acute.Const.Companion.RawUrl
import cat.moki.acute.client.NetClient
import kotlinx.parcelize.Parcelize
import java.util.Date


@Entity(primaryKeys = ["id", "server"])
@Parcelize
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
data class Song(
    val id: String,
    var server: String = "",
    val album: String?,
    val albumId: String?,
    val artist: String?,
    val artistId: String?,
    val bitRate: Int?,
    val contentType: String?,
    val coverArt: String?,
    val created: Date?,
    val duration: Int?,
    val isDir: Boolean,
    val isVideo: Boolean?,
    val parent: String?,
    val path: String?,
    val size: Int?,
    val suffix: String?,
    val title: String,
    val track: Int?,
    val discNumber: Int?,
    val type: String?,
    val year: Int?,
    val genre: String?
) : Parcelable {

//    constructor(
//        id: String,
//        album: String?,
//        albumId: String?,
//        artist: String?,
//        artistId: String?,
//        bitRate: Int?,
//        contentType: String?,
//        coverArt: String?,
//        created: Date?,
//        duration: Int?,
//        isDir: Boolean,
//        isVideo: Boolean?,
//        parent: String?,
//        path: String?,
//        size: Int?,
//        suffix: String?,
//        title: String,
//        track: Int?,
//        discNumber: Int?,
//        type: String?,
//        year: Int?,
//        genre: String?,
//
//        ) : this(
//        id,
//        album,
//        albumId,
//        artist,
//        artistId,
//        bitRate,
//        contentType,
//        coverArt,
//        created,
//        duration,
//        isDir,
//        isVideo,
//        parent,
//        path,
//        size,
//        suffix,
//        title,
//        track,
//        discNumber,
//        type,
//        year,
//        genre
//    )
//
//    fun setServer(serverId: String) {
//        server = serverId
//    }

    private fun setMetadata(builder: MediaMetadata.Builder) {
        builder.apply {
            setTitle(title)
            setArtist(artist)
            setAlbumTitle(album)
            setIsPlayable(true)
            setIsBrowsable(false)
            setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
        }
    }

    private fun setCover(builder: MediaMetadata.Builder) {
        coverArt?.let { builder.setArtworkUri(Uri.parse(NetClient.link(server).getCoverArtUrl(it))) }
    }

    fun makeBundle(rawUri: String, others: (data: Bundle) -> Unit = {}): Bundle {
        val data = Bundle()
        data.putParcelable(Const.Song, this@Song)
        data.putLong(Const.Duration, duration?.times(1000L) ?: 0L)
        data.putString(Const.Server, server)
        data.putString(CacheUrl, NetClient.link(server).getCachedStreamUrl(rawUri))
        data.putString(AlbumLocalMediaId, parent?.let { MediaId(server, MEDIA_TYPE_ALBUM, it).toString() })
        data.putString(RawUrl, rawUri)
        data.putString(CacheInfo, CacheTrackFile(serverId = server, trackId = id).pathWithoutDownload)
        if (rawUri.startsWith("file://"))
            data.putBoolean("isLocal", true)

        others(data)
        return data
    }

    private fun mediaMetadata(rawUri: String): MediaMetadata {
        return MediaMetadata.Builder().apply {
            setMetadata(this)
            setCover(this)
            setExtras(makeBundle(rawUri))
        }.build()
    }

    private fun setMediaUrl(builder: MediaItem.Builder, rawUri: String) {
        builder.setUri(NetClient.link(server).getCachedStreamUrl(rawUri))
    }

    private fun setMediaItemProps(builder: MediaItem.Builder) {
        builder.apply {
            setMediaId(id)
            setMimeType(contentType)
        }
    }

    fun buildMediaItem(
        rawUri: String = NetClient.link(server).getRawStreamUrl(id),
        fillToUri: Boolean = false
    ): MediaItem {
        return MediaItem.Builder().apply {
            setMediaItemProps(this)
            if (fillToUri) setMediaUrl(this, rawUri)
            setMediaMetadata(mediaMetadata(rawUri))
        }.build()
    }

    val mediaItem: MediaItem
        get() = buildMediaItem(fillToUri = true)

    @Deprecated("use mediaItem", ReplaceWith("mediaItem"))
    val mediaItemWithoutUri
        get() = buildMediaItem()
}

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg song: Song)

    @Delete
    fun delete(song: Song)

    @Query("DELETE FROM song where id=:id and server=:serverId")
    fun delete(id: String, serverId: String)

    @Query("SELECT * FROM song where id in (:songId) and server in (:serverId)")
    fun getAllIn(serverId: List<String>, songId: List<String>): List<Song>

    @Query("SELECT * FROM song")
    fun getAll(): List<Song>

    @Query("SELECT * FROM song where server=:serverId")
    fun getAllFromServer(serverId: String): List<Song>

    @Query("SELECT * FROM song where albumId=:id and server=:serverId")
    fun getAlbum(id: String, serverId: String): List<Song>

    @Query("SELECT * FROM song where id=:id and server=:serverId")
    fun get(id: String, serverId: String): Song?

    @Query("SELECT * FROM song where id in (:id) and server=:serverId")
    fun getIn(id: List<String>, serverId: String): List<Song>

    @Query("SELECT * FROM song where id in (:id)")
    fun getAllIn(id: List<String>): List<Song>

}

