package cat.moki.acute.components.library

import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import cat.moki.acute.components.LibraryViewModelLocal
import cat.moki.acute.components.NavControllerLocal
import cat.moki.acute.components.utils.ChangeableCoverPic
import cat.moki.acute.components.utils.TitleBracketScale
import cat.moki.acute.models.localMediaId
import cat.moki.acute.routes.Strings
import cat.moki.acute.routes.Strings.Companion.AlbumId
import kotlinx.coroutines.launch

enum class ViewBy {
    list, grid
}

@Composable
fun Library() {

//    val loadingError = rememberSaveable { mutableStateOf(false) }
//    suspend fun query() {
//        loadingError.value = false
//        library.getNextAlbums() ?: run {
//            loadingError.value = true
//        }
//    }

//    if (mode == ViewBy.grid) {
//        AlbumPreviewGrid(
//            onNavToAlbum = onNavToAlbum,
//        )
//    }
//    if (mode == ViewBy.list) {
//
//    }
    AlbumPreviewList()
}

@Composable
fun AlbumPreviewList() {
    val library = LibraryViewModelLocal.current

    if (library.albumList.isEmpty()) {
        LaunchedEffect(true) { library.getNextAlbums() }
    }
    val coroutineScope = rememberCoroutineScope()

    Log.d("TAG", "Library: ${library.loading.value}")
    LazyColumn {
        itemsIndexed(library.albumList, key = { key, album -> album.mediaId }) { index, album ->
            AlbumListItem(album = album)
            if (index == library.albumList.size - 10) {
                DisposableEffect(true) {
                    coroutineScope.launch { library.getNextAlbums() }
                    onDispose { library.lockReset() }
                }
            }
        }
        item {
            BoxWithConstraints(
                Modifier.fillMaxWidth()
            ) {
                if (!library.loaded.value) {
                    DisposableEffect(true) {
                        coroutineScope.launch { library.getNextAlbums() }
                        onDispose { library.lockReset() }
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

@Composable
fun AlbumListItem(album: MediaItem) {
    val navController = NavControllerLocal.current
    val library = LibraryViewModelLocal.current
    ListItem(
        modifier = Modifier
            .height(100.dp)
            .clickable {
                library.cacheDetail.value = album
                navController.navigate(Strings.Album.replace("{${AlbumId}}", album.localMediaId.toString())) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                }

            },
        leadingContent = {
            ChangeableCoverPic(modifier = Modifier.clip(shape = RoundedCornerShape(8.dp)), albumPic = album.mediaMetadata.artworkUri)
        },
        headlineContent = {
            TitleBracketScale(
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
//    Card(onClick = {
//        onNavToAlbum(album)
//    }) {
//        Log.i("TIME DEBUG", "AlbumListItem start: " + System.currentTimeMillis())
//
//
//        Log.i("TIME DEBUG", "AlbumListItem end: " + System.currentTimeMillis())
//
//    }
}

@Composable
fun AlbumPreviewGrid(onNavToAlbum: (MediaItem) -> Unit) {
    val library = LibraryViewModelLocal.current

    LazyVerticalStaggeredGrid(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        verticalItemSpacing = 8.dp,
        columns = StaggeredGridCells.Adaptive(minSize = 160.dp),
    ) {

        items(library.albumList) {
            AlbumGridItem(it, onNavToAlbum)
        }

        item {
            BoxWithConstraints(
                Modifier
                    .width(150.dp)
                    .height(200.dp)
            ) {
                if (!library.loaded.value) {
                    LaunchedEffect(true) { library.getNextAlbums() }
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

@Composable
fun AlbumGridItem(album: MediaItem, onNavToAlbum: (MediaItem) -> Unit) {
    Column(modifier = Modifier.clickable { onNavToAlbum(album) }) {
        ChangeableCoverPic(modifier = Modifier.clip(shape = RoundedCornerShape(8.dp)), albumPic = album.mediaMetadata.artworkUri)
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
//    Card(onClick = {
//        onNavToAlbum(album)
//    }) {
//
//    }

}
