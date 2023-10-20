package cat.moki.acute

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList
import kotlin.math.ceil

enum class ViewBy {
    list, grid
}

val MediaItemListSaver = listSaver<MutableList<MediaItem>, MediaItem>(
    save = { stateList ->
        if (stateList.isNotEmpty()) {
            val first = stateList.first()
            if (!canBeSaved(first)) {
                throw IllegalStateException("${first::class} cannot be saved. By default only types which can be stored in the Bundle class can be saved.")
            }
        }
        stateList.toList()
    },
    restore = { it.toMutableStateList() }
)

class LibraryLibrary : ViewModel() {
    var _albumList = mutableStateListOf<MediaItem>()
    val _lastLoad = mutableIntStateOf(1)
    val albumList: List<MediaItem>
        get() = _albumList.toImmutableList()
    val lastLoad: Int
        get() = _lastLoad.intValue

    private var _root: MediaItem? = null
    private suspend fun getRoot(browser: MediaBrowser): MediaItem {
        _root ?: run {
            _root = browser.getLibraryRoot(null).await().value
        }
        return _root!!
    }

    fun getAlbums(browser: MediaBrowser, page: Int, size: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            val root = getRoot(browser = browser)
            val result = browser.getChildren(root.mediaId, page, size, null).await()
            if (result.resultCode == LibraryResult.RESULT_SUCCESS) {
                Log.d("reachBottom", "Library: query succeeded")
                result.value?.let {
                    Log.d("reachBottom", "Library:response length ${it.size}")
                    _albumList.addAll(it)
                    _lastLoad.intValue = it.size
                    Log.d("reachBottom", "Library:albumList length ${_albumList.size}")
                } ?: run { Log.w("Library UI", "Library: resultCode ${result.resultCode} but result.value is null") }

            } else {
                Log.e("TAG", "getAlbums: request error")
            }
        }

    }
}

@Composable
fun Library(libraryLibrary: LibraryLibrary, browser: MediaBrowser, onNavToAlbum: (MediaItem) -> Unit, mode: ViewBy = ViewBy.list) {
    val pageSize = 10
    fun pageCount(): Int = ceil(libraryLibrary.albumList.size.toDouble() / pageSize.toDouble()).toInt()
    fun query() = libraryLibrary.getAlbums(browser = browser, pageCount(), pageSize)

    if (mode == ViewBy.grid) {
        AlbumPreviewGrid(
            albumList = libraryLibrary.albumList,
            onNavToAlbum = onNavToAlbum,
            allLoaded = libraryLibrary.lastLoad == 0,
            load = { query() })
    }
    if (mode == ViewBy.list) {
        AlbumPreviewList(
            albumList = libraryLibrary.albumList,
            onNavToAlbum = onNavToAlbum,
            allLoaded = libraryLibrary.lastLoad == 0,
            load = { query() }
        )
    }

}

@Composable
fun AlbumPreviewList(albumList: List<MediaItem>, onNavToAlbum: (MediaItem) -> Unit, allLoaded: Boolean, load: () -> Unit) {
    LazyColumn {
        items(albumList) { album ->
            AlbumListItem(album = album, onNavToAlbum = onNavToAlbum)
        }
        item {
            BoxWithConstraints(
                Modifier.fillMaxWidth()
            ) {
                if (!allLoaded) {
                    LaunchedEffect(true) { load() }
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = ">_<",
                            fontSize = 40.sp,
                            modifier = Modifier.align(Alignment.BottomCenter),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalGlideComposeApi::class)
@Composable
fun AlbumListItem(album: MediaItem, onNavToAlbum: (MediaItem) -> Unit) {
    Card(onClick = {
        onNavToAlbum(album)
    }) {

        ListItem(
            modifier = Modifier.height(100.dp),
            leadingContent = {
                Card(shape = RoundedCornerShape(8.dp)) {
                    GlideImage(
                        model = album.mediaMetadata.artworkUri,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                    ) {
                        it
                            .error(R.drawable.ic_baseline_library_music_24)
                            .placeholder(R.drawable.ic_baseline_downloading_24)
                    }
                }


            },
            headlineContent = {
                Text(
                    text = album.mediaMetadata.title.toString(),
                    fontSize = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = album.mediaMetadata.artist.toString(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
        )
    }
}

@Composable
fun AlbumPreviewGrid(albumList: List<MediaItem>, onNavToAlbum: (MediaItem) -> Unit, allLoaded: Boolean, load: () -> Unit) {

    LazyVerticalStaggeredGrid(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        verticalItemSpacing = 8.dp,
        columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
    ) {

        items(albumList) {
            AlbumGridItem(it, onNavToAlbum)
        }

        item {
            BoxWithConstraints(
                Modifier
                    .width(150.dp)
                    .height(200.dp)
            ) {
                if (!allLoaded) {
                    LaunchedEffect(true) { load() }
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Box(
                        modifier = Modifier
                            .height(100.dp)
                            .padding(top = 20.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = ">_<",
                            fontSize = 80.sp,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumGridItem(album: MediaItem, onNavToAlbum: (MediaItem) -> Unit) {
    Card(onClick = {
        onNavToAlbum(album)
    }) {
        Column() {
            Card(shape = RoundedCornerShape(0.dp)) {
                GlideImage(
                    model = album.mediaMetadata.artworkUri,
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                ) {
                    it
                        .error(R.drawable.ic_baseline_library_music_24)
                        .placeholder(R.drawable.ic_baseline_downloading_24)
                }

            }

            Text(
                text = album.mediaMetadata.albumTitle.toString(),
                fontSize = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(6.dp, 8.dp, 6.dp, 0.dp)
            )
            Text(
                text = album.mediaMetadata.albumArtist.toString(),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(6.dp, 4.dp, 6.dp, 10.dp)
            )
        }

    }

}
