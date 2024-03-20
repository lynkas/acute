package cat.moki.acute.client

import android.content.Context
import android.util.Log
import cat.moki.acute.data.AppDatabase
import cat.moki.acute.models.Album
import cat.moki.acute.models.MediaId
import cat.moki.acute.models.Playlist
import cat.moki.acute.models.Res
import cat.moki.acute.models.SearchResult2
import cat.moki.acute.models.SearchResult3
import retrofit2.Response
import retrofit2.http.Query

class OnlineClient(private val context: Context, private val serverId: String) : IQuery {
    val TAG = "OnlineClient"
    private suspend fun <A> requestFramework(
        request: suspend (api: Api) -> Response<Res>,
        unpack: (data: Res) -> A?,
        beforeFinal: (A) -> Unit = {},
        cache: (db: AppDatabase, data: A) -> Unit
    ): A {
        val response = request(NetClient.request(serverId))
        if (!response.isSuccessful) {
            Log.w(TAG, "requestFramework: response.isSuccessful is false")
            throw RequestFailedException()
        }

        val body = response.body()
        if (body == null) {
            Log.w(TAG, "requestFramework: body() is null")
            throw EmptyBodyException()
        }

        body.subsonicResponse.setServer(serverId)

        val data = unpack(body)
        if (data == null) {
            Log.w(TAG, "requestFramework: unpack data is null")
            throw EmptyBodyException()
        }

        beforeFinal(data)

        cache(AppDatabase.getInstance(context), data)
        return data
    }

    override suspend fun getAlbumList(
        type: String,
        size: Int,
        offset: Int,
        fromYear: Int?,
        toYear: Int?,
        genre: String?,
        musicFolderId: String?
    ): List<Album> {
        return requestFramework(
            request = { it.getAlbumList2(type, size, offset, fromYear, toYear, genre, musicFolderId) },
            unpack = { it.subsonicResponse.albumList2?.album },
            cache = { db, it -> db.album().insertAll(*it.toTypedArray()) }
        )
    }

    override suspend fun getAlbumDetail(id: String): Album {
        return requestFramework(
            request = { it.getAlbum(id) },
            unpack = { it.subsonicResponse.album },
            cache = { db, it ->
                db.album().insertAll(it)
                it.song?.toTypedArray()?.let { it1 -> db.song().insertAll(*it1) }
            }
        )
    }

    override suspend fun getPlaylists(): List<Playlist> {
        return requestFramework(
            request = { it.getPlaylists() },
            unpack = { it.subsonicResponse.playlists?.playlist },
            cache = { db, it ->
                db.playlist().insertAll(*it.toTypedArray())
                it.forEach { db.song().insertAll(*it.entry.toTypedArray()) }
            }
        )
    }

    override suspend fun getPlaylist(id: String): Playlist {
        return requestFramework(
            request = { it.getPlaylist(id) },
            unpack = { it.subsonicResponse.playlist },
            cache = { db, it ->
                db.playlist().insertAll(it)
                db.song().insertAll(*it.entry.toTypedArray())
            }
        )

    }

    override suspend fun search2(
        query: String,
        artistCount: Int,
        artistOffset: Int,
        albumCount: Int,
        albumOffset: Int,
        songCount: Int,
        songOffset: Int,
        musicFolderId: String?,
    ): SearchResult2 {
        return requestFramework(
            request = {
                it.search2(
                    query,
                    artistCount,
                    artistOffset,
                    albumCount,
                    albumOffset,
                    songCount,
                    songOffset,
                    musicFolderId,
                )
            },
            unpack = { it.subsonicResponse.searchResult2 },
            cache = { db, it ->
                db.song().insertAll(*it.song.toTypedArray())
                db.album().insertAll(*it.album.toTypedArray())
                it.album.forEach {
                    it.song?.toTypedArray()?.let { it1 -> db.song().insertAll(*it1) }
                }
                db.artist().insertAll(*it.artist.toTypedArray())
            }
        )
    }

    override suspend fun search3(
        query: String,
        artistCount: Int,
        artistOffset: Int,
        albumCount: Int,
        albumOffset: Int,
        songCount: Int,
        songOffset: Int,
        musicFolderId: String?,
    ): SearchResult3 {
        return requestFramework(
            request = {
                it.search3(
                    query,
                    artistCount,
                    artistOffset,
                    albumCount,
                    albumOffset,
                    songCount,
                    songOffset,
                    musicFolderId,
                )
            },
            unpack = { it.subsonicResponse.searchResult3 },
            cache = { db, it ->
                it.song?.let { it1 -> db.song().insertAll(*it1.toTypedArray()) }
                it.album?.let { it1 -> db.album().insertAll(*it1.toTypedArray()) }
                it.album?.forEach {
                    it.song?.toTypedArray()?.let { it1 -> db.song().insertAll(*it1) }
                }
                it.artist?.let { it1 -> db.artist().insertAll(*it1.toTypedArray()) }
            }
        )
    }

}
