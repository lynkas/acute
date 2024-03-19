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
import cat.moki.acute.AcuteApplication
import java.io.IOException

@OptIn(UnstableApi::class)
fun cacheDataSource(context: Context): CacheDataSource.Factory {
    val TAG = "cacheDataSource"
    return CacheDataSource.Factory().apply {
        Log.d(TAG, "onCreate: ${LocalSimpleCache.cache}")
        setCache(LocalSimpleCache.cache)
        setUpstreamDataSourceFactory(DefaultDataSource.Factory(context).apply {
            setTransferListener(object : TransferListener {
                override fun onTransferInitializing(source: DataSource, dataSpec: DataSpec, isNetwork: Boolean) {
                    if (!AcuteApplication.application.useOnlineSource) {
                        throw RuntimeException("should not use network")
                    }
                }

                override fun onTransferStart(source: DataSource, dataSpec: DataSpec, isNetwork: Boolean) {
                }

                override fun onBytesTransferred(
                    source: DataSource,
                    dataSpec: DataSpec,
                    isNetwork: Boolean,
                    bytesTransferred: Int
                ) {
                }

                override fun onTransferEnd(source: DataSource, dataSpec: DataSpec, isNetwork: Boolean) {
                }

            })
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