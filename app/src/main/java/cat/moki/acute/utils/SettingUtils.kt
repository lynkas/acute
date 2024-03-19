package cat.moki.acute.utils

import com.google.common.collect.ImmutableList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64


class Setting {
    val v = "1";

//    companion object {
//        private val credentialListType = object : TypeToken<List<Credential>>() {}.type
//        private val gson = Gson()
//    }
//
//    private var rawCredentials: String by DelegateString()
//
//    var credentials: ImmutableList<Credential>
//        get() {
//            return if (rawCredentials.isEmpty()) {
//                ImmutableList.of()
//            } else {
//                gson.fromJson(rawCredentials, credentialListType)
//            }
//        }
//        set(value) {
//            gson.toJson(value)
//        }
//

    var internetWifiOnly: Boolean by DelegateBoolean()
    var addDuplicateSongInPlaylist: Boolean by DelegateBoolean()
    var smallEndBrackets: Boolean by DelegateBoolean()

}

