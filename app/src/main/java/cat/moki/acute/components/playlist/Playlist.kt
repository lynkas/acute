package cat.moki.acute.components.playlist

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_PLAYLIST
import cat.moki.acute.AcuteApplication
import cat.moki.acute.components.LibraryViewModelLocal
import cat.moki.acute.components.NavControllerLocal
import cat.moki.acute.models.Credential
import cat.moki.acute.models.MediaId
import cat.moki.acute.routes.Strings

@Composable
fun PlaylistList(credential: Credential) {
    val TAG = "PlaylistList"
    val library = LibraryViewModelLocal.current
    val playlist = remember { mutableStateListOf<MediaItem>() }
    LaunchedEffect(true) {
        playlist.clear()
        playlist.addAll(library.getPlaylists(credential.id).keys)
    }
    Log.d(TAG, "PlaylistList: $playlist")
    LazyColumn {
        item { ListItem(headlineContent = { Text(credential.displayName) }) }
        itemsIndexed(playlist, key = { _, item -> item.mediaId }) { _, item -> PlaylistListItem(item) }

    }
}

@Composable
fun Playlist() {
    Column(modifier = Modifier.fillMaxWidth()) {

        val navController = NavControllerLocal.current
        ListItem(modifier = Modifier.clickable {
            navController.navigate(Strings.Playlist + MediaId(Strings.Local, MEDIA_TYPE_PLAYLIST, "0"))
        }, headlineContent = { Text("Local Cache") })

        AcuteApplication.application.servers.forEach {
            PlaylistList(it)
        }

    }
}