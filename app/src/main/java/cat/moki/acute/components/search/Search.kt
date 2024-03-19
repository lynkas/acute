package cat.moki.acute.components.search

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.LibraryResult
import cat.moki.acute.components.LibraryViewModelLocal
import cat.moki.acute.components.NavControllerLocal
import cat.moki.acute.components.PlayerViewModelLocal
import cat.moki.acute.models.Album
import cat.moki.acute.models.localMediaId
import cat.moki.acute.routes.Strings
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

@Composable
fun Search() {
    val TAG = "Search"
    val scope = rememberCoroutineScope()
    val library = LibraryViewModelLocal.current
    val navController = NavControllerLocal.current
    val player = PlayerViewModelLocal.current
    val tab = remember { mutableStateOf(TabItem.General) }
    val loading = remember { mutableStateOf(false) }
    val query = remember { mutableStateOf("") }
    val searchResult = remember { mutableStateListOf<MediaItem>() }
    LaunchedEffect(library.searchResultTime.value) {
        Log.d(TAG, "Search: " + library.searchResultTime.value.toString())
        if (!loading.value) return@LaunchedEffect
        else loading.value = false
        searchResult.clear()
        val result = library.browser.value.getSearchResult(query.value, 0, Int.MAX_VALUE, null).await()
        if (result.resultCode == LibraryResult.RESULT_SUCCESS)
            result.value?.let { searchResult.addAll(it) }
        else Log.w(TAG, "Search: ${result.resultCode}, request error")
    }
    Column(modifier = Modifier.fillMaxHeight()) {
        SearchBar(query, search = {
            loading.value = true
            searchResult.clear()
            if (query.value.isEmpty()) return@SearchBar
            Log.d(TAG, "Search: SearchBar")

            scope.launch {
                library.browser.value.search(query.value, null)
            }
        }) {
            Tabs(tab)
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                searchResult.filter { if (tab.value == TabItem.General) true else it.mediaMetadata.mediaType == tab.value.type }.map {
                    item {
                        when (it.mediaMetadata.mediaType) {
                            MediaMetadata.MEDIA_TYPE_ALBUM -> AlbumItem(it, query.value) {
                                navController.navigate(Strings.Album.replace("{${Strings.AlbumId}}", it.localMediaId.toString()))
                            }

                            MediaMetadata.MEDIA_TYPE_MUSIC -> SongItem(it, query.value) {
                                player.addMediaItem(it)
                                player.play()
                            }

                            MediaMetadata.MEDIA_TYPE_ARTIST -> ArtistItem(it, query.value, {})
                            else -> {}
                        }
                        Divider(modifier = Modifier.fillMaxWidth())

                    }

                }

            }
        }
    }

}