package cat.moki.acute.services

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import cat.moki.acute.AcuteApplication

object LocalSimpleCache {

    private var _dataBase: StandaloneDatabaseProvider? = null
    private var _cache: Cache? = null


    val database: StandaloneDatabaseProvider
        @OptIn(UnstableApi::class)
        get() {
            if (_dataBase == null) {
                _dataBase = StandaloneDatabaseProvider(AcuteApplication.application.context)
            }
            return _dataBase!!
        }
    val cache: Cache
        @OptIn(UnstableApi::class)
        get() {
            _cache ?: run {
                _cache = SimpleCache(AcuteApplication.application.audioCacheDir, NoOpCacheEvictor(), database)
            }
            return _cache!!
        }


}