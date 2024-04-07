package cat.moki.acute

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.cache.ContentMetadata
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.DefaultDownloaderFactory
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.scheduler.Requirements
import cat.moki.acute.client.LocalClient
import cat.moki.acute.models.CacheTrackFile
import cat.moki.acute.models.Credential
import cat.moki.acute.models.MediaId
import cat.moki.acute.services.LocalSimpleCache
import cat.moki.acute.services.cacheDataSource
import cat.moki.acute.services.registerInternetChange
import cat.moki.acute.utils.Setting
import cat.moki.acute.utils.Storage
import cat.moki.acute.utils.gson
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.Executors
import kotlin.io.path.createDirectories


@SuppressLint("UnsafeOptInUsageError")
class AcuteApplication : Application() {

    companion object {
        val TAG = "AcuteApplication"

        lateinit var application: AcuteApplication
        var internet = mutableStateOf(false)
        var metered = mutableStateOf(true)

        val useInternet: Boolean
            get() = internet.value && !metered.value

        @androidx.annotation.OptIn(UnstableApi::class)
        fun fullyCached(cacheTrackFile: CacheTrackFile): Boolean {
            val contentSize =
                LocalSimpleCache.cache.getContentMetadata(cacheTrackFile.pathWithoutDownload).get(ContentMetadata.KEY_CONTENT_LENGTH, -1)
            Log.d(TAG, "fullyCached: contentSize: ${contentSize}")
            return contentSize > 0 && LocalSimpleCache.cache.isCached(
                cacheTrackFile.pathWithoutDownload,
                0,
                contentSize
            )
        }

        val useOnlineSource
            get() = internet.value
                    && (!metered.value || !application.settings.internetWifiOnly)

    }

    fun serverMetadataUseInternet(serverId: String): Boolean {
        if (application.storage.serverConfiguration[serverId]?.onlyUseLocalMetaData == true) return false
        return useOnlineSource
    }

    val useOnlineSource
        get() = internet.value
                && (!metered.value || !application.settings.internetWifiOnly)

    private lateinit var accountManager: AccountManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager
    val cachedMediaItems = mutableStateListOf<MediaItem>()
    val settings = Setting()
    val storage = Storage()

    val serverMap = mutableStateMapOf<String, Credential>()
    val servers = mutableStateListOf<Credential>()
    val accounts = mutableMapOf<String, Account>()
    val localCoverCacheUpdateTime = mutableLongStateOf(System.currentTimeMillis())
    val defaultPlaylistMap = mutableStateOf<Map<String, String>>(emptyMap())

    private val _hash = MessageDigest.getInstance("SHA1")
    lateinit var audioCacheDir: File
    private lateinit var trackCoverDir: File

    @OptIn(ExperimentalStdlibApi::class)
    private fun hash(s: String?): String {
        return _hash.digest(s.toString().toByteArray()).toHexString()
    }

    val downloadManager: DownloadManager by lazy {
        val manager = DownloadManager(
            this,
            DefaultDownloadIndex(LocalSimpleCache.database),
            DefaultDownloaderFactory(cacheDataSource(this), Executors.newFixedThreadPool(/* nThreads= */ 6))
        )
        if (!useInternet) manager.requirements = Requirements(Requirements.NETWORK_UNMETERED)
        manager
    }

    val context: Context
        get() = this;

    override fun onCreate() {
        super.onCreate()
        accountManager = AccountManager.get(this)
        application = this;
        audioCacheDir = File(filesDir, "audio")
        trackCoverDir = File(cacheDir, "cover")
        trackCoverDir.toPath().createDirectories()

        internet()
        initServers()
        updateDefaultServer()
        initImageLoad()
    }


    fun initImageLoad() {

        val pipelineConfig =
            OkHttpImagePipelineConfigFactory
                .newBuilder(
                    this, OkHttpClient.Builder()
                        .apply {
                            addNetworkInterceptor {
                                val serverId = uriToTrackCache(it.request().url.toString().toUri())?.serverId
                                Log.d(TAG, "initImageLoad: serverId ${serverId}")
                                Log.d(
                                    TAG,
                                    "initImageLoad: !serverMetadataUseInternet(serverId) ${serverId != null && !serverMetadataUseInternet(serverId)}"
                                )
                                if (serverId != null && !serverMetadataUseInternet(serverId)) throw IOException("not allowed to use internet")
                                it.proceed(it.request())
                            }
                        }
                        .build()
                )
                .setDiskCacheEnabled(true)
                .setMainDiskCacheConfig(DiskCacheConfig.newBuilder(this).apply {
                    this.setMaxCacheSize(Long.MAX_VALUE)
                    this.setMaxCacheSizeOnLowDiskSpace(Long.MAX_VALUE)
                    this.setMaxCacheSizeOnVeryLowDiskSpace(Long.MAX_VALUE)
                }.build())
                .build()

        Fresco.initialize(this, pipelineConfig)
    }


    fun initServers() {
        servers.clear()
        serverMap.clear()
        accounts.clear()

        for (account in accountManager.accounts) {
            try {
                val c = gson.fromJson(accountManager.getPassword(account), Credential::class.java)
                accounts[c.id] = account
                servers.add(c)
                serverMap[c.id] = c
            } catch (e: JsonSyntaxException) {
                accountManager.removeAccountExplicitly(account)
                Log.w(TAG, "initServers: $account can't be parsed, removed")
            }
        }
    }

    val defaultServer: Credential?
        get() {
            if (servers.isEmpty()) return null
            return serverMap[storage.defaultServer] ?: servers.first()
        }

    val defaultServerId: String?
        get() = defaultServer?.id

    fun addAccount(credential: Credential) {
        val url = credential.displayName
        val account = Account(url, "cat.moki.acute")
        val bundle = Bundle()
        accountManager.addAccountExplicitly(account, credential.toString(), bundle)
    }

    fun removeAccount(id: String) {
        accountManager.removeAccountExplicitly(accounts[id])
    }

    override fun onTerminate() {

        super.onTerminate()
//        connectivityManager.releaseNetworkRequest()
    }

    private fun internet() {
        val TAG = "networkCallback"
        registerInternetChange(this, onChange = { internet, metered ->
            AcuteApplication.internet.value = internet
            AcuteApplication.metered.value = metered
        })
    }


    @androidx.annotation.OptIn(UnstableApi::class)
    fun allCacheFiles(): List<CacheTrackFile> {
        return LocalSimpleCache.cache.keys.map { CacheTrackFile.fromPath(it) }
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    fun allCacheFinishedFiles(): List<CacheTrackFile> {
        return LocalSimpleCache.cache.keys.mapNotNull {
            val file = CacheTrackFile.fromPath(it)
            if (fullyCached(file)) file else null
        }
    }

    fun allCacheFileMediaItem(files: List<CacheTrackFile>): Map<CacheTrackFile, MediaItem?> {
        return files.associateWith { LocalClient(this, it.serverId).getSong(it.trackId)?.mediaItem }
    }


    fun updateDefaultServer() {
        defaultPlaylistMap.value = storage.defaultPlaylist
    }

    fun setDefaultPlaylist(serverId: String, playlistId: String) {
        val map = storage.defaultPlaylist.toMutableMap()
        map[serverId] = playlistId
        storage.defaultPlaylist = map
        updateDefaultServer()
    }


    fun coverPath(mediaId: MediaId): File {
        return File(trackCoverDir, mediaId.base64)
    }

    fun coverPathUri(mediaId: MediaId): Uri {
        return Uri.fromFile(coverPath(mediaId))
    }

    fun hasTrackCover(mediaId: MediaId?): Boolean {
        if (mediaId == null) return false
        return coverPath(mediaId).exists()
    }

    fun writeTrackCover(mediaId: MediaId, data: ByteArray) {
        coverPath(mediaId).writeBytes(data)
        localCoverCacheUpdateTime.longValue = System.currentTimeMillis()
    }

    fun uriToTrackCache(uri: Uri): CacheTrackFile? {
        val server = servers.find {
            val serverUri = it.server.toUri()
            return@find serverUri.port == uri.port &&
                    serverUri.host == uri.host &&
                    it.username == uri.getQueryParameter("u")
        } ?: return null

        return CacheTrackFile(server.id, uri.getQueryParameter("id")!!)

    }


}

