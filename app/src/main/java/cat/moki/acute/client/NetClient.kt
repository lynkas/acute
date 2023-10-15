package cat.moki.acute.client

import android.annotation.SuppressLint
import cat.moki.acute.AcuteApplication
import cat.moki.acute.BuildConfig
import cat.moki.acute.models.Res
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.io.FileInputStream
import java.util.Properties

object NetClient {
    @SuppressLint("SuspiciousIndentation")
    private val okHttpClient = OkHttpClient().newBuilder().addInterceptor {
        val request: HttpUrl = authInterceptor(it.request().url.newBuilder()).build()
        it.proceed(it.request().newBuilder().url(request).build())

    }.build()

    val BaseUrl = BuildConfig.api

    private fun authInterceptor(builder: HttpUrl.Builder): HttpUrl.Builder {

        return builder.addQueryParameter("u", BuildConfig.u)
            .addQueryParameter("p", BuildConfig.p)
            .addQueryParameter("v", BuildConfig.v)
            .addQueryParameter("c", BuildConfig.c)
            .addQueryParameter("f", BuildConfig.f)
    }

    private val client = Retrofit.Builder().apply {
        baseUrl(BaseUrl)
        client(okHttpClient)
        addConverterFactory(GsonConverterFactory.create())
    }.build().create(Api::class.java)

    fun request(): Api {
        return client
    }

    fun getCoverArtUrl(id: String, size: Int? = null): String {
        authInterceptor(BaseUrl.toHttpUrlOrNull()!!.newBuilder()).apply {

            addPathSegment("getCoverArt")
            addQueryParameter("id", id)
            if (size != null) addQueryParameter("size", size.toString())
            return build().toString()
        }
    }

    fun getStreamUrl(id: String?): String {
        authInterceptor(BaseUrl.toHttpUrlOrNull()!!.newBuilder()).apply {
            addPathSegment("stream")
            addQueryParameter("id", id)
            addQueryParameter("maxBitRate", "192")
            return AcuteApplication.proxy.getProxyUrl(build().toString())
        }
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
}

