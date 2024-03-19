package cat.moki.acute.utils

import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.UnstableApi

const val TAG = "test listener"

val testListener = @UnstableApi object : Player.Listener {
    override fun onEvents(player: Player, events: Player.Events) {
        super.onEvents(player, events)
//        Log.d(TAG, "onEvents: ")
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        super.onTimelineChanged(timeline, reason)
        Log.d(TAG, "onTimelineChanged: ")
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        Log.d(TAG, "onMediaItemTransition: ")
    }

    override fun onTracksChanged(tracks: Tracks) {
        super.onTracksChanged(tracks)
        Log.d(TAG, "onTracksChanged: ${tracks.groups.size}")
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        Log.d(TAG, "onMediaMetadataChanged: ")
    }

    override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onPlaylistMetadataChanged(mediaMetadata)
        Log.d(TAG, "onPlaylistMetadataChanged: ")
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        super.onIsLoadingChanged(isLoading)
        Log.d(TAG, "onIsLoadingChanged: ")
    }

    @Deprecated("Deprecated in Java")
    override fun onLoadingChanged(isLoading: Boolean) {
        super.onLoadingChanged(isLoading)
        Log.d(TAG, "onLoadingChanged: ")
    }

    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
        super.onAvailableCommandsChanged(availableCommands)
        Log.d(TAG, "onAvailableCommandsChanged: ")
    }

    override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
        super.onTrackSelectionParametersChanged(parameters)
        Log.d(TAG, "onTrackSelectionParametersChanged: ")
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        Log.d(TAG, "onPlayerStateChanged: ")
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        Log.d(TAG, "onPlaybackStateChanged: $playbackState")
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        super.onPlayWhenReadyChanged(playWhenReady, reason)
        Log.d(TAG, "onPlayWhenReadyChanged: ")
    }

    override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
        super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
        Log.d(TAG, "onPlaybackSuppressionReasonChanged: ")
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        Log.d(TAG, "onIsPlayingChanged: ")
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        super.onRepeatModeChanged(repeatMode)
        Log.d(TAG, "onRepeatModeChanged: ")
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        super.onShuffleModeEnabledChanged(shuffleModeEnabled)
        Log.d(TAG, "onShuffleModeEnabledChanged: ")
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Log.d(TAG, "onPlayerError: ")
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        super.onPlayerErrorChanged(error)
        Log.d(TAG, "onPlayerErrorChanged: ")
    }

    override fun onPositionDiscontinuity(reason: Int) {
        super.onPositionDiscontinuity(reason)
        Log.d(TAG, "onPositionDiscontinuity: ")
    }

    override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
        super.onPositionDiscontinuity(oldPosition, newPosition, reason)
        Log.d(TAG, "onPositionDiscontinuity: ")
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        super.onPlaybackParametersChanged(playbackParameters)
        Log.d(TAG, "onPlaybackParametersChanged: ")
    }

    override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
        super.onSeekBackIncrementChanged(seekBackIncrementMs)
        Log.d(TAG, "onSeekBackIncrementChanged: ")
    }

    override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
        super.onSeekForwardIncrementChanged(seekForwardIncrementMs)
        Log.d(TAG, "onSeekForwardIncrementChanged: ")
    }

    override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
        super.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs)
        Log.d(TAG, "onMaxSeekToPreviousPositionChanged: ")
    }

    override fun onAudioSessionIdChanged(audioSessionId: Int) {
        super.onAudioSessionIdChanged(audioSessionId)
        Log.d(TAG, "onAudioSessionIdChanged: ")
    }

    override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
        super.onAudioAttributesChanged(audioAttributes)
        Log.d(TAG, "onAudioAttributesChanged: ")
    }

    override fun onVolumeChanged(volume: Float) {
        super.onVolumeChanged(volume)
        Log.d(TAG, "onVolumeChanged: ")
    }

    override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
        super.onSkipSilenceEnabledChanged(skipSilenceEnabled)
        Log.d(TAG, "onSkipSilenceEnabledChanged: ")
    }

    override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
        super.onDeviceInfoChanged(deviceInfo)
        Log.d(TAG, "onDeviceInfoChanged: ")
    }

    override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
        super.onDeviceVolumeChanged(volume, muted)
        Log.d(TAG, "onDeviceVolumeChanged: ")
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        super.onVideoSizeChanged(videoSize)
        Log.d(TAG, "onVideoSizeChanged: ")
    }

    override fun onSurfaceSizeChanged(width: Int, height: Int) {
        super.onSurfaceSizeChanged(width, height)
        Log.d(TAG, "onSurfaceSizeChanged: ")
    }

    override fun onRenderedFirstFrame() {
        super.onRenderedFirstFrame()
        Log.d(TAG, "onRenderedFirstFrame: ")
    }

    override fun onCues(cues: MutableList<Cue>) {
        super.onCues(cues)
        Log.d(TAG, "onCues: ")
    }

    override fun onCues(cueGroup: CueGroup) {
        super.onCues(cueGroup)
        Log.d(TAG, "onCues: ")
    }

    override fun onMetadata(metadata: Metadata) {
        super.onMetadata(metadata)
        Log.d(TAG, "onMetadata: ")
    }
}