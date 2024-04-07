package cat.moki.acute.components.utils

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.media3.common.util.UnstableApi
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.NetClient
import cat.moki.acute.models.MediaId
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.fresco.FrescoImage
import java.io.File


//import com.skydoves.landscapist.ImageOptions
//import com.skydoves.landscapist.glide.GlideImage


//@kotlin.OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ChangeableCoverPic(
    modifier: Modifier = Modifier,
    trackPic: String? = null,
    albumPic: String? = null
) {
    val TAG = "ChangeableCoverPic"
    val context = LocalContext.current
    var skipTrackPic by rememberSaveable { mutableStateOf(trackPic == null) }
    Log.d(TAG, "ChangeableCoverPic: skipA ${skipTrackPic}")

    LaunchedEffect(trackPic) { if (trackPic == null) skipTrackPic = true }
    LaunchedEffect(skipTrackPic, trackPic, albumPic) {
        Log.d(TAG, "ChangeableCoverPic: track ${trackPic}")
        Log.d(TAG, "ChangeableCoverPic: album ${albumPic}")
        Log.d(TAG, "ChangeableCoverPic: skip ${skipTrackPic}")
    }
    val uri = if (!skipTrackPic) trackPic else albumPic
    FrescoImage(
        imageUrl = uri,
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f),
        imageOptions = ImageOptions(
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center
        ),
        imageRequest = {
            ImageRequestBuilder
                .newBuilderWithSource(uri?.toUri())
                .setProgressiveRenderingEnabled(true)

        },
    )
//    GlideImage(
//        model = if (!skipTrackPic) trackPic else albumPic,
//        modifier = modifier
//            .fillMaxHeight()
//            .aspectRatio(1f),
//        contentDescription = "",
//        contentScale = ContentScale.Crop
////        loading = placeholder(Icons.Filled.Downloading.get),
////        failure = {
////            Log.d("ChangeableCoverPic", "ChangeableCoverPic: ${it.reason}")
////            Log.d("ChangeableCoverPic", "ChangeableCoverPic: ${it.errorDrawable}")
////            Icon(Icons.Filled.LibraryMusic, contentDescription = "")
////        }
//    ) {
//        it.listener(object : RequestListener<Drawable> {
//            override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>, p3: Boolean): Boolean {
////                if (trackPic == p1) skipTrackPic = true
//                Log.w(TAG, "onLoadFailed: ", p0)
//                p0?.printStackTrace()
//                Log.d(TAG, "onLoadFailed: ${p1}")
//                p2.getSize { i, j ->
//                    Log.d(TAG, "onLoadFailed: ${i} $j")
//
//                }
//                Log.d(TAG, "onLoadFailed: ${p2}")
//                return false
//            }
//
//            override fun onResourceReady(p0: Drawable, p1: Any, p2: Target<Drawable>?, p3: com.bumptech.glide.load.DataSource, p4: Boolean): Boolean {
//                return false
//            }
//
//        }).onlyRetrieveFromCache(!AcuteApplication.application.useOnlineSource && !(!skipTrackPic && trackPic.toString().startsWith("/")))
//
//    }
}

@OptIn(UnstableApi::class)
@Composable
fun AutoCoverPic(
    modifier: Modifier = Modifier,
    trackId: MediaId? = null,
    albumId: MediaId? = null
) {

    fun trackCoverUri(trackId: MediaId?): String? {
        return if (AcuteApplication.application.hasTrackCover(trackId)) {
            AcuteApplication.application.coverPathUri(trackId!!).toString()
        } else {
            null
        }
    }

    val localCover = remember { mutableStateOf(trackCoverUri(trackId)) }
    Log.d("TAGaaa", "AutoCoverPic: ${localCover.value}")
    Log.d("TAGaaa", "AutoCoverPic: albumId ${albumId}")

    LaunchedEffect(AcuteApplication.application.localCoverCacheUpdateTime.longValue, trackId, albumId) {
        localCover.value = trackCoverUri(trackId)
    }
    ChangeableCoverPic(
        modifier,
        localCover.value,
        albumId?.let { albumId -> Uri.parse(NetClient.link(albumId.serverId).getCoverArtUrl(albumId.itemId)).toString() })


}