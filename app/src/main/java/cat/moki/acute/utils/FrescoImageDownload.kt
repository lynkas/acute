package cat.moki.acute.utils

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.core.ImagePipeline
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun frescoImageDownload(
    uri: Uri,
    imageRequest: ImageRequest? = ImageRequest.fromUri(uri),
    imagePipeline: ImagePipeline = Fresco.getImagePipeline()
): Bitmap {
    return suspendCancellableCoroutine {
        val dataSource = imagePipeline.fetchDecodedImage(imageRequest, it.context)
        dataSource.subscribe(
            object : BaseBitmapDataSubscriber() {
                override fun onNewResultImpl(bitmap: Bitmap?) {
                    if (bitmap == null) {
                        it.resumeWithException(IOException("empty bitmap"))
                    } else {
                        Log.d(TAG, "loadBitmap: ${bitmap.byteCount}")
                        it.resume(bitmap)
                    }
                }

                override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>) {
                    dataSource.failureCause?.let { it1 -> it.resumeWithException(it1) }
                        ?: it.resumeWithException(IOException("empty exception"))
                }

            },
            UiThreadImmediateExecutorService.getInstance()
        )
    }
}