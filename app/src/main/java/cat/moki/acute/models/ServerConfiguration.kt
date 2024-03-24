package cat.moki.acute.models

import cat.moki.acute.utils.gson
import java.util.Date

data class ServerConfiguration(
    val lastTimeFullyCache: Date? = null,
    val onlyUseLocalMetaData: Boolean = false

) {

    companion object {
        fun from(content: String): ServerConfiguration {
            return gson.fromJson(content, ServerConfiguration::class.java)
        }
    }

    override fun toString(): String {
        return gson.toJson(this)
    }


}