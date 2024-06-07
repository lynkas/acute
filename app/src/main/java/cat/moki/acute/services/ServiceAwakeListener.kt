package cat.moki.acute.services

import android.util.Log
import androidx.media3.common.Player
import androidx.media3.common.Tracks

class ServiceAwakeListener(val wakeLockRequire: () -> Unit, val wakeLockRelease: () -> Unit) : Player.Listener {
    val TAG = "ServiceAwakeListener"
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        Log.d(TAG, "onIsPlayingChanged: $isPlaying")
        wakeLockRequire()
//        if (isPlaying) wakeLockRequire()
//        else wakeLockRelease()
    }
}