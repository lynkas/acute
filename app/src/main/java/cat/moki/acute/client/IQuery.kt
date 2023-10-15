package cat.moki.acute.client

import cat.moki.acute.models.Album

interface IQuery {
    suspend fun getAlbumList(
        type: String = "newest", size: Int = 10, offset: Int = 0,
        fromYear: Int? = null,
        toYear: Int? = null,
        genre: String? = null,
        musicFolderId: String? = null
    ): List<Album>

    suspend fun getAlbumDetail(id: String): Album
}