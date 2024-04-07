package cat.moki.acute.services

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import cat.moki.acute.AcuteApplication
import okhttp3.Call
import okhttp3.OkHttpClient
import java.io.IOException

@OptIn(UnstableApi::class)
fun cacheDataSource(context: Context): CacheDataSource.Factory {
    val TAG = "cacheDataSource"
    val client = OkHttpClient.Builder().addNetworkInterceptor {
        Log.d(TAG, "cacheDataSource: it.request().url ${it.request().url}")
        val response = if (AcuteApplication.useOnlineSource) {
            it.proceed(it.request())
        } else {
            throw IOException("not allowed to use internet ")
        }
        if (response.body?.contentType()?.type != "audio") {
            Log.w(TAG, "cacheDataSource: ${it.request().url} is not an audio")
            Log.w(TAG, "cacheDataSource: content size ${response.body?.contentLength()}")
            Log.w(TAG, "cacheDataSource: content ${response.body?.byteString()}")
            throw IOException("not a valid audio file")
        }
        response
    }.build()
    return CacheDataSource.Factory().apply {
        Log.d(TAG, "onCreate: ${LocalSimpleCache.cache}")
        setCache(LocalSimpleCache.cache)
        setUpstreamDataSourceFactory(OkHttpDataSource.Factory {
            client.newCall(it)
        })
        setCacheKeyFactory { dataSpec ->
            if (dataSpec.uri.toString().isEmpty()) {
                throw DataSourceException(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
            }
            Log.d(TAG, "cacheDataSource: ${dataSpec.uri}")
            AcuteApplication.application.uriToTrackCache(dataSpec.uri)?.pathWithoutDownload!!
        }


    }
}