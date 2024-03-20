package cat.moki.acute.components.utils

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.NetClient
import cat.moki.acute.models.MediaId
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target


//import com.skydoves.landscapist.ImageOptions
//import com.skydoves.landscapist.glide.GlideImage


//@kotlin.OptIn(ExperimentalGlideComposeApi::class)
@kotlin.OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ChangeableCoverPic(
    modifier: Modifier = Modifier,
    trackPic: Any? = null,
    albumPic: Any? = null
) {
    val TAG = "ChangeableCoverPic"
    val context = LocalContext.current
//    Image(
//        painter = rememberAsyncImagePainter(model = trackPic ?: albumPic,
//            imageLoader = ImageLoader.Builder(context).diskCache {
//                DiskCache.Builder()
//                    .directory(context.cacheDir.resolve("image_cache"))
//                    .build()
//            }.build(),
//            onError = {
//                Log.d(TAG, "ChangeableCoverPic: image error")
//            }),
//        contentDescription = null,
//    )
//    AsyncImage(
//        model = trackPic ?: albumPic,
//        contentDescription = null,
//        imageLoader = ImageLoader.Builder(context).diskCache {
//            DiskCache.Builder()
//                .directory(context.cacheDir.resolve("image_cache"))
//                .build()
//        }.build(),
//        onError = {
//            Log.d(TAG, "ChangeableCoverPic: image error")
//        }
//    )
    GlideImage(
        model = trackPic ?: albumPic,
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f),
        contentDescription = "",
        contentScale = ContentScale.Crop
//        loading = placeholder(Icons.Filled.Downloading.get),
//        failure = {
//            Log.d("ChangeableCoverPic", "ChangeableCoverPic: ${it.reason}")
//            Log.d("ChangeableCoverPic", "ChangeableCoverPic: ${it.errorDrawable}")
//            Icon(Icons.Filled.LibraryMusic, contentDescription = "")
//        }
    ) {
        it.listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(p0: GlideException?, p1: Any?, p2: Target<Drawable>, p3: Boolean): Boolean {
                Log.w(TAG, "onLoadFailed: ", p0)
                Log.d(TAG, "onLoadFailed: ${p1}")
                return false
            }

            override fun onResourceReady(p0: Drawable, p1: Any, p2: Target<Drawable>?, p3: com.bumptech.glide.load.DataSource, p4: Boolean): Boolean {
                return false
            }

        }).onlyRetrieveFromCache(!AcuteApplication.application.useOnlineSource)

    }
}

@OptIn(UnstableApi::class)
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