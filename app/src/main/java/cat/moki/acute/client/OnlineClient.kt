package cat.moki.acute.client

import android.content.Context
import cat.moki.acute.data.AppDatabase
import cat.moki.acute.models.Album

class OnlineClient(val context: Context) : IQuery {
    override suspend fun getAlbumList(
        type: String,
        size: Int,
        offset: Int,
        fromYear: Int?,
        toYear: Int?,
        genre: String?,
        musicFolderId: String?
    ): List<Album> {
        val data = NetClient.request()
            .getAlbumList2(type, size, offset, fromYear, toYear, genre, musicFolderId)
        if (data.isSuccessful) {
            data.body()?.subsonicResponse?.albumList2?.album?.let {
                AppDatabase.getInstance(context).album().insertAll(*it.toTypedArray())
                return it
            }
            throw EmptyBodyException()
        }
        throw RequestFailedException()
    }

    override suspend fun getAlbumDetail(id: String): Album {
        val data = NetClient.request().getAlbum(id)
        if (data.isSuccessful) {
            data.body()?.subsonicResponse?.album?.let {
                AppDatabase.getInstance(context).album().insertAll(it)
                it.song?.let { songs ->
                    AppDatabase.getInstance(context).song().insertAll(*songs.toTypedArray())
                }
                return it
            }
            throw EmptyBodyException()
        }
        throw RequestFailedException()
    }
}
