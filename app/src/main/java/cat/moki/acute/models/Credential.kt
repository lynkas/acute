package cat.moki.acute.models

import android.os.Parcelable
import cat.moki.acute.utils.gson
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

@Parcelize
data class Credential(
    val id: String,
    val server: String,
    val username: String,
    val token: String,
    val salt: String
) : Parcelable {

    companion object {
        private val random = SecureRandom()

        @OptIn(ExperimentalStdlibApi::class)
        fun make(server: String, username: String, password: String): Credential {
            random.setSeed(random.generateSeed(10))
            val byte = ByteArray(20)
            random.nextBytes(byte)
            val salt = Base64.getUrlEncoder().withoutPadding().encodeToString(byte)
            val token = MessageDigest.getInstance("MD5").digest((password + salt).toByteArray()).toHexString()
            return Credential(server = server, username = username, token = token, salt = salt, id = UUID.randomUUID().toString())
        }


    }

    override fun toString(): String {
        return gson.toJson(this)
    }

    val displayName
        get() = this.server.toHttpUrl().newBuilder().apply {
            username(username)
        }.toString().split(Regex("://"), 2).last()

}

