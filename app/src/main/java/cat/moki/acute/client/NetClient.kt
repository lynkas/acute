package cat.moki.acute.client

import android.annotation.SuppressLint
import android.provider.MediaStore.Audio.Media
import android.util.Log
import androidx.media3.common.MediaMetadata
import cat.moki.acute.AcuteApplication
import cat.moki.acute.BuildConfig
import cat.moki.acute.models.CacheTrackFile
import cat.moki.acute.models.Credential
import cat.moki.acute.models.MediaId
import cat.moki.acute.models.Res
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

object NetClient {
    @SuppressLint("SuspiciousIndentation")
    private fun okHttpClient(credentialInterceptor: (HttpUrl.Builder) -> Unit) = OkHttpClient.Builder().addInterceptor {
        val request: HttpUrl = it.request().url.newBuilder().apply {
            baseInterceptor(this)
            credentialInterceptor(this)
        }.build()
        it.proceed(it.request().newBuilder().url(request).build())
    }.build()

//    val BaseUrl = BuildConfig.api

    private fun authInterceptor(builder: HttpUrl.Builder): HttpUrl.Builder {

        return builder.addQueryParameter("u", BuildConfig.u)
            .addQueryParameter("p", BuildConfig.p)
    }

    private fun baseInterceptor(builder: HttpUrl.Builder): HttpUrl.Builder {
        return builder.addQueryParameter("v", "1.14.0")
            .addQueryParameter("c", "acute")
            .addQueryParameter("f", "json")
    }

    private val clients = mutableMapOf<String, Api>()


    fun request(serverId: String): Api {
        var client = clients[serverId]
        if (client == null) {
            val credential = AcuteApplication.application.serverMap[serverId]
                ?: throw IllegalArgumentException("server id $serverId does not exist")
            client = request(credential = credential)
        }
        return client
    }

    fun request(credential: Credential): Api {
        return Retrofit.Builder().apply {
            baseUrl(credential.server)
            client(okHttpClient {
                addCredentialParam(credential = credential, it)
            })
            addConverterFactory(GsonConverterFactory.create())
        }.build().create(Api::class.java)
    }

    private fun addCredentialParam(credential: Credential, httpUrl: HttpUrl.Builder): HttpUrl.Builder {
        return baseInterceptor(
            httpUrl.addQueryParameter("u", credential.username)
                .addQueryParameter("s", credential.salt)
                .addQueryParameter("t", credential.token)
        )
    }

    fun _link(credential: Credential): NetLink {
        val builder = credential.server.toHttpUrlOrNull()?.newBuilder()?.apply {
            addCredentialParam(credential = credential, this)
        } ?: throw IllegalArgumentException("server ${credential.server} is not a valid link")
        return NetLink(builder, credential.id)
    }

    fun link(serverId: String): NetLink {
        val credential = AcuteApplication.application.serverMap[serverId]
            ?: throw IllegalArgumentException("server id $serverId does not exist")
        return _link(credential)
    }


}

class NetLink(val builder: HttpUrl.Builder, val serverId: String) {
    fun getCoverArtUrl(id: String, size: Int? = 512): String {
        builder.apply {
            addPathSegment("getCoverArt")
            addQueryParameter("id", id)
            if (size != null)
                addQueryParameter("size", size.toString())
            return build().toString()
        }
    }

    fun getStreamUrl(id: String, cacheInfo: CacheTrackFile = CacheTrackFile(serverId = serverId, trackId = id), cacheLink: Boolean = true): String {
        builder.apply {
            addPathSegment("stream")
            addQueryParameter("id", id)
//            addQueryParameter("maxBitRate", "192")
            fragment(cacheInfo.pathWithoutDownload)
            val link = build().toString()
            Log.d("TAG", "getStreamUrl: cacheLink ${cacheLink}")
            Log.d("TAG", "getStreamUrl: link ${link}")
            return link
        }
    }

    fun getRawStreamUrl(id: String, cacheInfo: CacheTrackFile = CacheTrackFile(serverId = serverId, trackId = id)): String {
        return builder.apply {
            addPathSegment("stream")
            addQueryParameter("id", id)
//            addQueryParameter("maxBitRate", "192")
            fragment(cacheInfo.pathWithoutDownload)
        }.build().toString()
    }

    fun getCachedStreamUrl(originalUrl: String): String {
//        assert(!originalUrl.startsWith("http://127.0.0.1"))
//
//        if (originalUrl.startsWith("file:/")) return originalUrl

        return originalUrl

    }
}


interface Api {
    @GET("ping.view")
    suspend fun ping(): Response<Res>

    @GET("getAlbumList")
    suspend fun getAlbumList(
        @Query("type") type: String = "newest",
        @Query("size") size: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("fromYear") fromYear: Int? = null,
        @Query("toYear") toYear: Int? = null,
        @Query("genre") genre: String? = null,
        @Query("musicFolderId") musicFolderId: String? = null
    ): Response<Res>

    @GET("getAlbumList2")
    suspend fun getAlbumList2(
        @Query("type") type: String = "newest",
        @Query("size") size: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("fromYear") fromYear: Int? = null,
        @Query("toYear") toYear: Int? = null,
        @Query("genre") genre: String? = null,
        @Query("musicFolderId") musicFolderId: String? = null
    ): Response<Res>

    @GET("getAlbum")
    suspend fun getAlbum(
        @Query("id") id: String,
    ): Response<Res>

    @GET("getPlaylists")
    suspend fun getPlaylists(): Response<Res>


    @GET("getPlaylist")
    suspend fun getPlaylist(
        @Query("id") id: String,
    ): Response<Res>

    @GET("createPlaylist")
    suspend fun updatePlaylist(
        @Query("playlistId") playlistId: String? = null,
        @Query("songId") songIds: List<String>? = null
    ): Response<Res>

    @GET("createPlaylist")
    suspend fun createPlaylist(
        @Query("name") name: String? = null,
        @Query("songId") songIds: List<String>? = null
    ): Response<Res>

    @GET("updatePlaylist")
    suspend fun updatePlaylist(
        @Query("playlistId") playlistId: String,
        @Query("name") name: String? = null,
        @Query("comment") comment: String? = null,
        @Query("public") boolean: Boolean? = null,
        @Query("songIdToAdd") addIds: List<String>? = null,
        @Query("songIndexToRemove") removeIndex: List<Int>? = null
    ): Response<Res>

    @GET("deletePlaylist")
    suspend fun deletePlaylist(
        @Query("id") id: String
    ): Response<Res>

    @GET("startScan")
    suspend fun startScan(): Response<Res>

    @GET("getScanStatus")
    suspend fun getScanStatus(): Response<Res>

    @GET("search2")
    suspend fun search2(
        @Query("query") query: String,
        @Query("artistCount") artistCount: Int = 20,
        @Query("artistOffset") artistOffset: Int = 0,
        @Query("albumCount") albumCount: Int = 20,
        @Query("albumOffset") albumOffset: Int = 0,
        @Query("songCount") songCount: Int = 20,
        @Query("songOffset") songOffset: Int = 0,
        @Query("musicFolderId") musicFolderId: String? = null,
    ): Response<Res>

    @GET("search3")
    suspend fun search3(
        @Query("query") query: String,
        @Query("artistCount") artistCount: Int = 20,
        @Query("artistOffset") artistOffset: Int = 0,
        @Query("albumCount") albumCount: Int = 20,
        @Query("albumOffset") albumOffset: Int = 0,
        @Query("songCount") songCount: Int = 20,
        @Query("songOffset") songOffset: Int = 0,
        @Query("musicFolderId") musicFolderId: String? = null,
    ): Response<Res>


}

