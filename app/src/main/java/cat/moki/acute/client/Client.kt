package cat.moki.acute.client

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import cat.moki.acute.models.Res
import cat.moki.acute.models.SubsonicResponse
import com.bumptech.glide.Glide
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type


class EmptyBodyException : Exception()
class RequestFailedException : Exception()


object Client {
    var networkMetered = mutableStateOf(false)

    enum class Type {
        Local,
        Online
    }

    fun store(context: Context, type: Type? = Type.Online): IQuery {
        val client = when (type) {
            Type.Online -> online(context = context)
            else -> when (networkMetered.value) {
                true -> local(context = context)
                false -> online(context = context)
            }
        }
        return client
    }

    private fun online(context: Context): IQuery {
        return OnlineClient(context = context)
    }

    private fun local(context: Context): IQuery {
        return LocalClient(context = context)
    }

}


class ResConverter : Converter.Factory() {
    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val envelopedType = TypeToken.getParameterized(Res::class.java, type).type
        val delegate: Converter<ResponseBody, Res> =
            retrofit.nextResponseBodyConverter(this, envelopedType, annotations)
        return Converter<ResponseBody, SubsonicResponse> {
            delegate.convert(it)!!.subsonicResponse
        }
    }
}
