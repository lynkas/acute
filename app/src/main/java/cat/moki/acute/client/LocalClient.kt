package cat.moki.acute.client

import android.content.Context
import cat.moki.acute.data.AppDatabase
import cat.moki.acute.models.Album


class LocalClient(val context: Context) : IQuery {
    override suspend fun getAlbumList(
        type: String,
        size: Int,
        offset: Int,
        fromYear: Int?,
        toYear: Int?,
        genre: String?,
        musicFolderId: String?
    ): List<Album> {
        return AppDatabase.getInstance(context).album().getAll(limit = size, offset = offset)
    }

    override suspend fun getAlbumDetail(id: String): Album {
        val songs = AppDatabase.getInstance(context).song().getAlbum(id)
        val album = AppDatabase.getInstance(context).album().get(id)
        album.song = songs
        return album
    }
}
