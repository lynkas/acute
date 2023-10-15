package cat.moki.acute

import android.app.Application
import android.content.Intent
import com.danikula.videocache.HttpProxyCacheServer
import com.danikula.videocache.file.FileNameGenerator


class AcuteApplication : Application() {
    companion object {
        private lateinit var _proxy: HttpProxyCacheServer
        val proxy
            get() = _proxy
    }

    override fun onCreate() {
        super.onCreate()
        _proxy = HttpProxyCacheServer.Builder(this).cacheDirectory(this.filesDir)
            .build()
//        Intent(this, Home::class.java).also { startActivity(it) }
    }
}