package cat.moki.acute

public class Const {
    companion object {
        val Duration = "DURATION"
        val Count = "COUNT"
        val Server = "SERVER"
        val CacheUrl = "CACHE_URL"
        val RawUrl = "RAW_URL"
        val CacheInfo = "CACHE_INFO"
        val Song = "SONG"
        val Songs = "SONGS"
        val Album = "ALBUM"
        val AlbumLocalMediaId = "ALBUM_LOCAL_MEDIA_ID"
        val Root = "ROOT"
        val Playlist = "PLAYLIST"
    }
}


enum class Command(val value: String) {
    UpdateInternetStatus("UpdateInternetStatus")

}


