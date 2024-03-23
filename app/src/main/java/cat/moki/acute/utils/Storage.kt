package cat.moki.acute.utils

import cat.moki.acute.models.ServerConfiguration
import com.google.gson.reflect.TypeToken

class Storage {
    private var defaultPlaylistMap by DelegateString()
    private var localPlaylist by DelegateString()
    private var _serverConfiguration by DelegateString()

    private val stringListType = object : TypeToken<List<String>>() {}.type
    private val stringStringMapType = object : TypeToken<Map<String, String>>() {}.type
    private val stringServerConfiguration = object : TypeToken<Map<String, ServerConfiguration>>() {}.type

    var mediaId by DelegateString()
    var defaultServer by DelegateString()
    var loop by DelegateInteger()
    var shuffle by DelegateBoolean()


    var playlist: List<String>
        get() {
            val playlistString = localPlaylist
            if (playlistString.isNotEmpty())
                return gson.fromJson(localPlaylist, stringListType)
            return listOf()
        }
        set(value) {
            localPlaylist = gson.toJson(value)
        }

    var defaultPlaylist: Map<String, String>
        get() = if (defaultPlaylistMap.isBlank()) emptyMap()
        else gson.fromJson(defaultPlaylistMap, stringStringMapType)
        set(value) {
            defaultPlaylistMap = gson.toJson(value)
        }

    var serverConfiguration: Map<String, ServerConfiguration>
        get() {
            return if (_serverConfiguration.isEmpty()) {
                mapOf()
            } else {
                gson.fromJson(_serverConfiguration, stringServerConfiguration)
            }
        }
        set(value) {
            _serverConfiguration = gson.toJson(value)
        }

    fun updateServerConfiguration(id: String, config: ServerConfiguration) {
        serverConfiguration = serverConfiguration.toMutableMap().apply { put(id, config) }
    }

}