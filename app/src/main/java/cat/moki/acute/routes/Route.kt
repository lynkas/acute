package cat.moki.acute.routes

class Strings {
    companion object {
        const val Main = "main/"
        const val Library = Main + "library/"
        const val AlbumId = "albumID"
        const val Album = "${Library}album/{${AlbumId}}"

        const val Playlist = Main + "playlist/"
        const val ServerId = "serverId"
        const val Id = "id"

        const val PlaylistDetail = Playlist + "{${Id}}"
        const val Local = "local"
        const val Setting = "setting/"
        const val Search = "search/"
        const val SettingFiles = "${Setting}files/"

        const val Download = "download/"

    }

}