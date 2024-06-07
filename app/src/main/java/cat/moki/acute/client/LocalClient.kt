package cat.moki.acute.client

import android.content.Context
import android.util.Log
import cat.moki.acute.data.AppDatabase
import cat.moki.acute.models.Album
import cat.moki.acute.models.MediaId
import cat.moki.acute.models.Playlist
import cat.moki.acute.models.SearchResult2
import cat.moki.acute.models.SearchResult3
import cat.moki.acute.models.Song


class LocalClient(val context: Context, val serverId: String) : IQuery {
    override suspend fun getAlbumList(
        type: String,
        size: Int,
        offset: Int,
        fromYear: Int?,
        toYear: Int?,
        genre: String?,
        musicFolderId: String?
    ): List<Album> {
        if (serverId == MediaId.RootString) {
            return AppDatabase.getInstance(context).album().getAll(limit = size, offset = offset)
        }
        return AppDatabase.getInstance(context).album().getAllWithServer(limit = size, offset = offset, serverId = serverId)
    }

    override suspend fun getAlbumDetail(id: String): Album {
        val songs = AppDatabase.getInstance(context).song().getAlbum(id, serverId = serverId)
        Log.d("TAG", "getAlbumDetail: ${songs.size}")
        val album = AppDatabase.getInstance(context).album().get(id, serverId = serverId)
        album.song = songs
        return album
    }

    override suspend fun getPlaylists(): List<Playlist> {
        return AppDatabase.getInstance(context).playlist().getAll()
    }

    fun getAllSongsIn(serverId: List<String>, trackId: List<String>): List<Song> {
        return AppDatabase.getInstance(context).song().getAllIn(serverId, trackId)
    }

    override suspend fun getPlaylist(id: String): Playlist {
        return AppDatabase.getInstance(context).playlist().get(id, serverId = serverId)
    }

    override suspend fun search2(
        query: String,
        artistCount: Int,
        artistOffset: Int,
        albumCount: Int,
        albumOffset: Int,
        songCount: Int,
        songOffset: Int,
        musicFolderId: String?
    ): SearchResult2 {
        TODO("Not yet implemented")
    }

    override suspend fun search3(
        query: String,
        artistCount: Int,
        artistOffset: Int,
        albumCount: Int,
        albumOffset: Int,
        songCount: Int,
        songOffset: Int,
        musicFolderId: String?
    ): SearchResult3 {
        TODO("Not yet implemented")
    }

    fun getSongIn(ids: List<String>): List<Song> {
        val songs = AppDatabase.getInstance(context).song().getIn(ids, serverId = serverId)
        val orderById = ids.withIndex().associate { (index, it) -> it to index }
        return songs.sortedBy { orderById[it.id] }
    }

    fun getSong(id: String): Song? {
        return AppDatabase.getInstance(context).song().get(id, serverId = serverId)
    }

    fun getSongInAllServer(ids: List<String>): List<Song> {
        val songs = AppDatabase.getInstance(context).song().getAllIn(ids)
        val orderById = ids.withIndex().associate { (index, it) -> it to index }
        return songs.sortedBy { orderById[it.id] }
    }


}
