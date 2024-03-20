package cat.moki.acute.models

import android.util.Log
import androidx.media3.common.MediaItem
import cat.moki.acute.Const

val TAG = "MediaItem"

val MediaItem.album: Album
    get() = this.mediaMetadata.extras?.run {
        classLoader = Album::class.java.classLoader
        val _album: Album = getParcelable(Const.Album)!!
        Log.d("MediaItem.album: Album", "album id ${_album.id} songs ${_album.song?.size}")
        _album
    }!!

val MediaItem.songs: List<Song>?
    get() = this?.mediaMetadata?.extras?.run {
        classLoader = Song::class.java.classLoader
        Log.d(TAG, ": getParcelableArrayList(Const.Songs) ${getParcelableArrayList<Song>(Const.Songs)}")
        getParcelableArrayList(Const.Songs)
    }

val MediaItem.song: Song
    get() = this.mediaMetadata.extras?.run {
        Log.d("TAG", "MediaItem.song: ${this}")
        classLoader = Song::class.java.classLoader
        getParcelable(Const.Song)
    }!!

val MediaItem.playlist: Playlist
    get() = this.mediaMetadata.extras?.run {
        classLoader = Playlist::class.java.classLoader
        getParcelable(Const.Playlist)
    }!!

val MediaItem.serverId: String
    get() = this.mediaMetadata.extras?.run {
        getString(Const.Server)
    }!!

val MediaItem.songFilled: MediaItem
    get() {
        Log.d(TAG, "MediaItem.songFilled: ${this}")

        return this.song.buildMediaItem(fillToUri = true)
    }

val MediaItem.duration: Long
    get() = this.mediaMetadata.extras?.run {
        getLong(Const.Duration)
    }!!

val MediaItem.localMediaId: MediaId
    get() = MediaId(this.serverId, this.mediaMetadata.mediaType ?: 0, mediaId)

val MediaItem.albumLocalMediaId: MediaId?
    get() = this.mediaMetadata.extras?.run {
        getString(Const.AlbumLocalMediaId)?.toMediaId()
    }!!

val MediaItem.cacheUrl: String
    get() = this.mediaMetadata.extras!!.getString(Const.CacheUrl)!!

val MediaItem.cacheInfo: CacheTrackFile
    get() = CacheTrackFile.fromPath(this.mediaMetadata.extras!!.getString(Const.CacheInfo)!!)

val MediaItem.rawUrl: String
    get() = this.mediaMetadata.extras!!.getString(Const.RawUrl)!!

val MediaItem.isLocal: Boolean
    get() = this.mediaMetadata.extras!!.getBoolean("isLocal")

