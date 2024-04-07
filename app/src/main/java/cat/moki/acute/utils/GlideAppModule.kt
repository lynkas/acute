package cat.moki.acute.utils
//
//import android.content.Context
//import com.bumptech.glide.Glide
//import com.bumptech.glide.Registry
//import com.bumptech.glide.annotation.GlideModule
//import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
//import com.bumptech.glide.load.model.GlideUrl
//import com.bumptech.glide.module.AppGlideModule
//import okhttp3.OkHttpClient
//import java.io.IOException
//import java.io.InputStream
//import java.util.concurrent.TimeUnit
//
//
//@GlideModule
//class MyGlideAppModule : AppGlideModule() {
//    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
//        val client: OkHttpClient = OkHttpClient.Builder()
//            .addInterceptor { chain ->
//                val res = chain.proceed(chain.request())
//                if (res.isSuccessful) {
//                    if (res.body?.contentType()?.type != "image") {
//                        throw IOException("request response is not valid image")
//                    }
//                }
//                res
//            }
//            .build()
//        val factory = OkHttpUrlLoader.Factory(client)
//        registry.replace(GlideUrl::class.java, InputStream::class.java, factory)
//    }
//}