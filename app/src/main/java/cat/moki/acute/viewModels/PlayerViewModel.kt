package cat.moki.acute.viewModels

import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionCommand
import cat.moki.acute.AcuteApplication
import cat.moki.acute.Command
import cat.moki.acute.client.LocalClient
import cat.moki.acute.client.NetClient
import cat.moki.acute.components.player.readMediaList
import cat.moki.acute.models.Song
import cat.moki.acute.models.TAG
import cat.moki.acute.models.cacheInfo
import cat.moki.acute.models.localMediaId
import cat.moki.acute.models.song
import cat.moki.acute.models.toMediaId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.Collections


@OptIn(UnstableApi::class)
class PlayerViewModel : ViewModel() {
    val currentTime = mutableLongStateOf(0)
    val loadingTime = mutableLongStateOf(0)
    val playQueue: SnapshotStateList<MediaItem> = mutableStateListOf()
    val displayPlayQueue: List<MediaItem>
        get() {
            if (currentMediaIndex.value == null) return playQueue.toList()
            val newQueue = playQueue.toMutableList()
            Collections.rotate(newQueue, -currentMediaIndex.value!! + 1)
            Log.d(TAG, ": ${newQueue.mapIndexed { index, mediaItem -> index }}}")
            return newQueue.toList()
        }

    val isPlaying = mutableStateOf(false)
    val isLoading = mutableStateOf(false)
    val isMovingBar = mutableStateOf(false)
    val totalTime = mutableLongStateOf(1)
    val currentMediaItem = mutableStateOf<MediaItem?>(null)
    val currentMediaIndex = mutableStateOf<Int?>(null)
    val currentMediaMetadata = mutableStateOf<MediaMetadata?>(null)
    val currentMediaLocalItem = mutableStateOf<Song?>(null)
    lateinit var browser: MutableState<MediaBrowser>
    val currentMediaCacheAvailable = mutableStateOf(false)
    val inited = mutableStateOf(false)
    var timeJob: Job? = null
    var lastPlayStartTime = mutableLongStateOf(0)
    var playStartDuration = mutableLongStateOf(0)
    var shuffle = mutableStateOf(false)
    var loop = mutableIntStateOf(0)

    @UnstableApi
    val storage = AcuteApplication.application.storage

    @OptIn(UnstableApi::class)
    fun init() {
        if (browser.value.mediaItemCount == 0)
            restore()

        isPlaying.value = browser.value.isPlaying
        isLoading.value = browser.value.isLoading
        currentMediaItem.value = browser.value.currentMediaItem
        currentMediaIndex.value = browser.value.currentMediaItemIndex
        currentMediaMetadata.value = browser.value.currentMediaItem?.mediaMetadata
        Log.d("TAG", "init: ${browser.value.currentMediaItem?.song}")
        currentMediaLocalItem.value = browser.value.currentMediaItem?.song
        setPlayTime()
        browser.value.apply {
            currentTime.longValue = currentPosition
            loadingTime.longValue = bufferedPosition
            loadPlaylist()
            totalTime.longValue = currentMediaItem?.song?.duration?.toLong()?.times(1000) ?: 1L
            currentMediaItem?.let {
                currentMediaCacheAvailable.value = AcuteApplication.fullyCached(it.cacheInfo)
            }
            shuffle.value = shuffleModeEnabled
            loop.value = repeatMode
        }

        timeJob = timeRunning()
    }

    fun setPlayTime() {
        playStartDuration.longValue = browser.value.currentPosition
        lastPlayStartTime.longValue = System.currentTimeMillis()
    }

    fun setBrowser(browser: MediaBrowser) {
        this.browser = mutableStateOf(browser)
        init()
        inited.value = true
        browser.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(newIsPlaying: Boolean) {
                super.onIsPlayingChanged(newIsPlaying)
                isPlaying.value = newIsPlaying
                Log.d("timeJob", "onIsPlayingChanged: ${timeJob}")
                setPlayTime()
                timeJob?.cancel()
                timeJob = if (newIsPlaying) {
                    timeRunning()
                } else {
                    null
                }
            }

            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                super.onPositionDiscontinuity(oldPosition, newPosition, reason)
                currentTime.longValue = newPosition.positionMs
            }

            @OptIn(UnstableApi::class)
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                Log.d("asdasd", "onMediaItemTransition: ")
                super.onMediaItemTransition(mediaItem, reason)
                totalTime.longValue = mediaItem?.song?.duration?.toLong()?.times(1000) ?: 1L
                currentMediaItem.value = mediaItem
                AcuteApplication.application.storage.mediaId = mediaItem?.localMediaId.toString()

                mediaItem?.let {
                    Log.d("TAG", "init: ${it.song}")
                    Log.d("TAG", "init: ${it}")
                    currentMediaLocalItem.value = it.song
                    currentMediaCacheAvailable.value = AcuteApplication.fullyCached(it.cacheInfo)

                }
            }

            override fun onIsLoadingChanged(newIsLoading: Boolean) {
                super.onIsLoadingChanged(newIsLoading)
                isLoading.value = newIsLoading
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                shuffle.value = shuffleModeEnabled
                storage.shuffle = shuffleModeEnabled
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                super.onRepeatModeChanged(repeatMode)
                loop.value = repeatMode
                storage.loop = repeatMode
            }

            override fun onTracksChanged(tracks: Tracks) {
                Log.d("asdasd", "onTracksChanged: ")
                super.onTracksChanged(tracks)
                currentMediaIndex.value = browser.currentMediaItemIndex
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                super.onTimelineChanged(timeline, reason)
                Log.d(TAG, "onTimelineChanged: ")
                currentMediaIndex.value = browser.currentMediaItemIndex
                loadPlaylist()

            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                currentMediaMetadata.value = mediaMetadata

                // write cover
                Log.d("TAG", "onMediaMetadataChanged: ${mediaMetadata.artworkData?.size}")
                mediaMetadata.artworkData?.let {
                    AcuteApplication.application.writeTrackCover(browser.currentMediaItem!!.localMediaId, it)
                }

            }
        })

    }

    fun loadPlaylist() {
        Log.d(TAG, "loadPlaylist: ")
        playQueue.clear()
        playQueue.addAll(readMediaList(browser.value))
        AcuteApplication.application.storage.playlist = playQueue.map { i -> i.localMediaId.toString() }

    }

    fun cleanTimeJob() {
        timeJob?.cancel()
        timeJob = null
    }

    private fun timeRunning(): Job {
        return viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                viewModelScope.launch { setPlayTime() }
                delay(500)
            }
        }
    }

    fun play() {
        if (currentMediaItem.value != null)
            if (AcuteApplication.useInternet || AcuteApplication.fullyCached(currentMediaItem.value!!.cacheInfo)) {
                browser.value.playWhenReady
                browser.value.prepare()
                browser.value.play()
            }
    }

    fun pause() {
        browser.value.pause()
    }

    fun toggle() {
        if (isPlaying.value) pause()
        else play()
    }

    fun play(index: Int) {
        browser.value.seekTo(index, 0)
        play()
    }

    fun indexOf(mediaItem: MediaItem): Int {
        return playQueue.indexOfFirst { it.localMediaId == mediaItem.localMediaId }
    }

    fun addToLast(mediaItem: MediaItem): Int {
        browser.value.addMediaItem(mediaItem)
        return browser.value.mediaItemCount - 1
    }

    fun addToPosition(mediaItem: MediaItem, position: Int): Int {
        browser.value.addMediaItem(position, mediaItem)
        return position
    }

    @OptIn(UnstableApi::class)
    fun addMediaItem(
        mediaItem: MediaItem,
        position: Int = browser.value.mediaItemCount,
        allowDuplicated: Boolean = AcuteApplication.application.settings.addDuplicateSongInPlaylist
    ): Int {
        val index = if (allowDuplicated) -1 else indexOf(mediaItem)
        val finalIndex = if (index == -1) position else index
        if (index == -1) addToPosition(mediaItem, finalIndex)
        return finalIndex
    }
//
//    fun addMediaItem(mediaItem: MediaItem): Int {
//        browser.value.addMediaItem(mediaItem)
//        Log.d("TAG", "addMediaItem: ${browser.value.mediaItemCount}")
//        return browser.value.mediaItemCount - 1
//    }
//
//    fun addMediaItem(mediaItem: MediaItem, position: Int? = null): Int {
//        if (AcuteApplication.application.settings.addDuplicateSongInPlaylist) {
//            val index = playQueue.indexOfFirst { it.mediaId == mediaItem.mediaId }
//            if (index != -1) return index
//        }
//        if (position == null) return addMediaItem(mediaItem)
//        browser.value.addMediaItem(position, mediaItem)
//        return position
//    }

    fun seek(index: Int? = null, time: Long? = null) {
        time?.let {
            currentTime.longValue = it
        }
        index?.let {
            currentMediaIndex.value = it
        }
        currentMediaIndex.value?.let {
            browser.value.seekTo(it, currentTime.longValue)
        }

    }

    val playTime: Long
        get() {
            if (isMovingBar.value) {
                return currentTime.longValue
            }
            return if (isPlaying.value) {
                System.currentTimeMillis() - lastPlayStartTime.longValue + playStartDuration.longValue
            } else {
                playStartDuration.longValue
            }
        }

    fun seekLast() {
        browser.value.seekTo(playQueue.size - 1, 0)
    }

    fun cover(albumCover: Boolean = false): Any? {
        val albumCoverUrl = currentMediaLocalItem.value?.let {
            it.coverArt?.let { it1 -> NetClient.link(it.server).getCoverArtUrl(it1) }
        }
        if (albumCover) return albumCoverUrl
        val mediaId = currentMediaItem.value?.mediaId?.toMediaId()
        return AcuteApplication.application.run {
            if (hasTrackCover(mediaId)) coverPath(mediaId!!)
            else if (currentMediaMetadata.value?.artworkData != null)
                currentMediaMetadata.value
            else albumCoverUrl
        }
    }

    override fun onCleared() {
        super.onCleared()
        cleanTimeJob()
    }

    val noPlayableMusic: Boolean
        get() {
            playQueue.forEach { mediaItem ->
                if (AcuteApplication.fullyCached(mediaItem.cacheInfo))
                    return false
            }
            return true
        }

    private fun restore() {
        Log.d(TAG, "restore: ")
        val songIds = AcuteApplication.application.storage.playlist
        val songs = songIds.mapNotNull {
            val mediaId = it.toMediaId()
            LocalClient(context = AcuteApplication.application.context, mediaId.serverId).getSong(mediaId.itemId)
        }

        browser.value.shuffleModeEnabled = storage.shuffle
        if (storage.loop in 0..2)
            browser.value.repeatMode = storage.loop

        val mediaItems = songs.map { it.mediaItem }
        mediaItems.forEach { browser.value.addMediaItem(it) }
        Log.d(TAG, "restore: viewModelScope.launch")
        viewModelScope.launch {
            Log.d(TAG, "restore: viewModelScope.launch")
            browser.value.sendCustomCommand(SessionCommand(Command.UpdateInternetStatus.value, Bundle()), Bundle()).await()
            Log.d(TAG, "restore: viewModelScope.launch")
            try {
                val mediaId = AcuteApplication.application.storage.mediaId.toMediaId()
                val index = mediaItems.indexOfFirst { it.localMediaId == mediaId }
                if (songs.isNotEmpty())
                    browser.value.seekToDefaultPosition(0.coerceAtLeast(index))
            } catch (e: Exception) {
                Log.w("TAG", "restore: ", e)
            }
        }

    }

}