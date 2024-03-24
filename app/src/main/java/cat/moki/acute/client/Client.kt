package cat.moki.acute.client

import android.content.Context
import cat.moki.acute.AcuteApplication
import cat.moki.acute.models.Res
import cat.moki.acute.models.SubsonicResponse
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type


class EmptyBodyException : Exception()
class RequestFailedException : Exception()


object Client {
//    var networkMetered = mutableStateOf(false)

    enum class Type {
        Local,
        Online
    }

    fun store(context: Context, serverId: String, type: Type? = null): IQuery {
        val client = when (type) {
            Type.Online -> online(context = context, serverId)
            else -> {
                val onlyLocal = AcuteApplication.application.storage.serverConfiguration[serverId]?.onlyUseLocalMetaData ?: false
                when (AcuteApplication.useInternet && !onlyLocal) {
                    true -> online(context = context, serverId)
                    false -> local(context = context, serverId)
                }
            }
        }
        return client
    }

    private fun online(context: Context, serverId: String): IQuery {
        return OnlineClient(context = context, serverId = serverId)
    }

    private fun local(context: Context, serverId: String): IQuery {
        return LocalClient(context = context, serverId = serverId)
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
