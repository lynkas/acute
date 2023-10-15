package cat.moki.acute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import cat.moki.acute.client.NetClient
import cat.moki.acute.models.Album
import cat.moki.acute.ui.theme.AcuteTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.guava.await

enum class ViewBy {
    list, grid
}

class LibraryOOld : ComponentActivity() {
//    private val library: LibraryViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var viewBy by rememberSaveable { mutableStateOf(ViewBy.list) }
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
            AcuteTheme {

                Surface() {
                    Scaffold(
                        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                        topBar = {
                            LargeTopAppBar(
                                title = { Text(text = "Library") },
                                scrollBehavior = scrollBehavior,
                                navigationIcon = {
                                    IconButton(onClick = { /* doSomething() */ }) {
                                        Icon(
                                            imageVector = Icons.Filled.Menu,
                                            contentDescription = "Localized description"
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = {
                                        viewBy =
                                            if (viewBy == ViewBy.list) ViewBy.grid else ViewBy.list
                                    }) {
                                        Icon(
                                            imageVector = if (viewBy == ViewBy.list) Icons.Filled.GridView else Icons.Filled.List,
                                            contentDescription = "Localized description"
                                        )
                                    }
                                },
                            )
                        },
//                        bottomBar = { Nav(navController) }
                    ) {
                        Box(modifier = Modifier.padding(it)) {
//                            if (viewBy == ViewBy.grid) {
//                                AlbumPreviewGrid(library = library)
//                            }
//                            if (viewBy == ViewBy.list) {
//                                AlbumPreviewList(library = library)
//                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumPreviewList(library: LibraryViewModel, onNavToAlbum: (String) -> Unit) {
    LazyColumn() {
        items(library.albumList) { album ->
            AlbumListItem(album = album, onNavToAlbum = onNavToAlbum)
        }
        item {
            BoxWithConstraints(
                Modifier.fillMaxWidth()
            ) {
                if (!library.loaded) {
                    val context = LocalContext.current
                    LaunchedEffect(true) {
                        library.get(context)
                    }
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
fun AlbumListItem(album: Album, onNavToAlbum: (String) -> Unit) {
    val context = LocalContext.current
    Card(onClick = {
        onNavToAlbum(album.id)
    }) {

        ListItem(
            modifier = Modifier.height(100.dp),
            leadingContent = {
                Card(shape = RoundedCornerShape(8.dp)) {
                    GlideImage(
                        model = album.coverArt?.let { NetClient.getCoverArtUrl(id = it) },
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
                    text = album.name,
                    fontSize = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                Text(
                    text = album.artist,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
        )
    }
}

@Composable
fun AlbumPreviewGrid(library: LibraryViewModel, onNavToAlbum: (String) -> Unit, browser: MediaBrowser) {
    val albumList = rememberSaveable(
        saver = listSaver<MutableList<MediaItem>, MediaItem>(
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
    ) { mutableListOf() }
    val page by rememberSaveable { mutableIntStateOf(0) }
    val pageSize by rememberSaveable { mutableIntStateOf(10) }
    val loading by rememberSaveable { mutableStateOf(false) }
    var root by rememberSaveable { mutableStateOf<MediaItem?>(null) }
    LaunchedEffect(true) {
        root = browser.getLibraryRoot(null).await().value
    }

    LazyVerticalStaggeredGrid(
        modifier = Modifier.padding(10.dp, 0.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
    ) {

        items(albumList) {
            AlbumPreview(it, onNavToAlbum)
        }

        item {
            BoxWithConstraints(
                Modifier
                    .width(150.dp)
                    .height(200.dp)
            ) {
                if (loading) {
                    val context = LocalContext.current
                    LaunchedEffect(true) {
                        if (root != null) {
                            val result = browser.getChildren(root!!.mediaId, page, pageSize, null).await().value
                            if (result != null) {
                                albumList.addAll(result)
                            }
                        }
                    }
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
fun AlbumPreview(album: MediaItem, onNavToAlbum: (String) -> Unit) {
    Card(onClick = {
        onNavToAlbum(album.mediaId)
    }) {
        Column() {
            Card(shape = RoundedCornerShape(8.dp)) {
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
                modifier = Modifier.padding(6.dp, 4.dp, 6.dp, 0.dp)
            )
            Text(
                text = album.mediaMetadata.albumArtist.toString(),
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(6.dp, 4.dp, 6.dp, 16.dp)
            )
        }

    }

}

@Preview(showBackground = true)
@Composable
fun Test() {

    Column {
        Text(
            text = "aaaaaaaaaaaaaaa",
            fontSize = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

