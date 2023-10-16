package cat.moki.acute

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.moki.acute.models.Album
import cat.moki.acute.models.Song
import cat.moki.acute.client.NetClient
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

class AlbumDetail : ComponentActivity() {
    private lateinit var album: Album

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        album = intent.getParcelableExtra("album")!!

        setContent {
        }
    }
}

@Composable
fun AlbumDetailComponent(
    scrollState: ScrollState = rememberScrollState(initial = 300),
    albumDetail: AlbumDetailData,
    addSong: (Song) -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {

        AlbumDetailHeadPic(albumDetail.album, scrollState.value)
        AlbumDetailHeadInfo(albumDetail.album)
        SongList(albumDetail.album, albumDetail.songs, albumDetail.sameArtist, addSong)

    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumDetailHeadPic(album: Album, scrollPosition: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
//            .padding(12.dp, 0.dp)

            .graphicsLayer { translationY = scrollPosition / 2f },
        shape = RectangleShape
    ) {
        GlideImage(
            model = NetClient.getCoverArtUrl(album.id),
            contentDescription = "",
            contentScale = ContentScale.FillWidth,
        )
    }
}

@Composable
fun AlbumTimeAndTracksInfo(seconds: Int, tracksNumber: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        val modifier = Modifier.size(18.dp)
        Icon(Icons.Outlined.Timer, "time", modifier = modifier)
        Text(text = seconds.formatSecond())
        Spacer(modifier = Modifier.width(6.dp))
        Icon(Icons.Outlined.MusicNote, "songs", modifier = modifier)
        Text(text = tracksNumber.toString())

    }
}

@Composable
fun AlbumDetailHeadInfo(album: Album) {
    Card(
        shape = RoundedCornerShape(bottomEnd = 0.dp, bottomStart = 0.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = album.name, fontSize = 26.sp, lineHeight = 32.sp)
                    Text(text = album.artist, fontSize = 18.sp)
                    AlbumTimeAndTracksInfo(album.duration, album.songCount)
                }
            }
        }
    }
}


@Composable
fun SongList(album: Album, songs: List<Song>, sameArtist: Boolean, addSong: (Song) -> Unit) {
    for ((index, song) in songs.withIndex()) {
        SongListItem(album, song = song, sameArtist = sameArtist, addSong)
        if (index + 1 != songs.size) Divider(Modifier.padding(horizontal = 12.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListItem(album: Album, song: Song, sameArtist: Boolean, addSong: (Song) -> Unit) {
    val context = LocalContext.current
    Card(shape = RoundedCornerShape(0.dp), onClick = {
        addSong(song)
    }) {
        ListItem(
            headlineContent = {
                Text(
                    text = song.title,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            supportingContent = {
                if (!sameArtist)
                    Text(
                        text = song.artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
            },
            trailingContent = {
                Text(
                    text = "${song.duration.formatSecond()} ",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
    }
}