package cat.moki.acute.components.utils

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.NetClient
import cat.moki.acute.models.MediaId
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import com.bumptech.glide.request.target.Target
import com.skydoves.landscapist.DataSource


@Composable
fun ChangeableCoverPic(
    modifier: Modifier = Modifier,
    trackPic: Any? = null,
    albumPic: Any? = null
) {
    val TAG = "ChangeableCoverPic"
    GlideImage(
        imageModel = { trackPic ?: albumPic },
        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
        requestBuilder = {
            Glide.with(LocalContext.current).asDrawable().listener(
                object : RequestListener<Drawable> {
                    override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>, p3: Boolean): Boolean {
                        Log.d(TAG, "onLoadFailed: $p0")
                        Log.d(TAG, "onLoadFailed: $p1")
                        Log.d(TAG, "onLoadFailed: $p2")
                        Log.d(TAG, "onLoadFailed: $p3")
                        return false
                    }

                    override fun onResourceReady(
                        p0: Drawable,
                        p1: Any,
                        p2: Target<Drawable>?,
                        p3: com.bumptech.glide.load.DataSource,
                        p4: Boolean
                    ): Boolean {
                        Log.d(TAG, "onResourceReady: $p0")
                        Log.d(TAG, "onResourceReady: $p1")
                        Log.d(TAG, "onResourceReady: $p2")
                        Log.d(TAG, "onResourceReady: $p3")
                        Log.d(TAG, "onResourceReady: $p4")
                        return false
                    }
                }
            )
        },
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f),
        loading = { Icon(Icons.Filled.Downloading, contentDescription = "") },
        failure = {
            Log.d("ChangeableCoverPic", "ChangeableCoverPic: ${it.reason}")
            Log.d("ChangeableCoverPic", "ChangeableCoverPic: ${it.errorDrawable}")
            Icon(Icons.Filled.LibraryMusic, contentDescription = "")
        }
    )
}

@Composable
fun AutoCoverPic(
    modifier: Modifier = Modifier,
    trackId: MediaId? = null,
    albumId: MediaId? = null
) {

    val localCover = remember { mutableStateOf(AcuteApplication.application.hasTrackCover(trackId)) }

    LaunchedEffect(AcuteApplication.application.localCoverCacheUpdateTime.longValue) {
        localCover.value = AcuteApplication.application.hasTrackCover(trackId)
    }
    return if (localCover.value && trackId != null && AcuteApplication.application.hasTrackCover(trackId)) {
        ChangeableCoverPic(modifier, AcuteApplication.application.coverPath(mediaId = trackId))
    } else {
        if (albumId != null) {
            ChangeableCoverPic(modifier, null, Uri.parse(NetClient.link(albumId.serverId).getCoverArtUrl(albumId.itemId)))
        } else {
            ChangeableCoverPic(modifier, null, null)
        }
    }


}