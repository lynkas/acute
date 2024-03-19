package cat.moki.acute.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

@Parcelize
data class RawCredential(
    val server: String = "",
    val username: String = "",
    val password: String = ""
) : Parcelable {
//
//    companion object {
//        private val random = SecureRandom()
//
//        @OptIn(ExperimentalStdlibApi::class)
//        fun toCredential(server: String, username: String, password: String): Credential {
//            random.setSeed(random.generateSeed(10))
//            val byte = ByteArray(20)
//            random.nextBytes(byte)
//            val salt = Base64.getUrlEncoder().withoutPadding().encodeToString(byte)
//            val token = MessageDigest.getInstance("MD5").digest((password + salt).toByteArray()).toHexString()
//            return Credential(server = server, username = username, token = token, salt = salt)
//        }
//    }

}

