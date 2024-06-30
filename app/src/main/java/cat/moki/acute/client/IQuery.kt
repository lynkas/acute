package cat.moki.acute.client

import cat.moki.acute.models.Album
import cat.moki.acute.models.Playlist
import cat.moki.acute.models.SearchResult2
import cat.moki.acute.models.SearchResult3
import cat.moki.acute.models.Song

interface IQuery {
    suspend fun getAlbumList(
        type: String = "newest", size: Int = 10, offset: Int = 0,
        fromYear: Int? = null,
        toYear: Int? = null,
        genre: String? = null,
        musicFolderId: String? = null
    ): List<Album>

    suspend fun getAlbumDetail(id: String): Album
    suspend fun getPlaylists(): List<Playlist>
    suspend fun getPlaylist(id: String): Playlist

    suspend fun search2(
        query: String,
        artistCount: Int = 20,
        artistOffset: Int = 0,
        albumCount: Int = 20,
        albumOffset: Int = 0,
        songCount: Int = 20,
        songOffset: Int = 0,
        musicFolderId: String? = null,
    ): SearchResult2

    suspend fun search3(
        query: String,
        artistCount: Int = 20,
        artistOffset: Int = 0,
        albumCount: Int = 20,
        albumOffset: Int = 0,
        songCount: Int = 20,
        songOffset: Int = 0,
        musicFolderId: String? = null,
    ): SearchResult3
}