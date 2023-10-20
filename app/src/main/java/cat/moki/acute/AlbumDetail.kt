package cat.moki.acute

import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import cat.moki.acute.models.Album
import cat.moki.acute.models.Song
import cat.moki.acute.client.NetClient
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cat.moki.acute.models.ToMediaItem

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
    scrollState: ScrollState = rememberScrollState(initial = 0),
    album: Album,
    addSong: (MediaItem) -> Unit,
) {
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {

        AlbumDetailHeadPic(NetClient.getCoverArtUrl(album.id), scrollState.value)
        AlbumDetailHeadInfo(
            artist = album.artist,
            title = album.realTitle ?: "",
            duration = album.duration,
            songCount = album.songCount
        )

        album.song?.let {
            for ((index, s) in it.withIndex()) {
                SongListItem(album, song = s, sameArtist = album.sameArtist, addSong)
                if (index + 1 != it.size) Divider(Modifier.padding(horizontal = 12.dp))
            }
        } ?: run {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AlbumDetailHeadPic(coverUrl: String, scrollPosition: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { translationY = scrollPosition / 2f },
        shape = RectangleShape
    ) {
        GlideImage(
            model = coverUrl,
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
fun AlbumDetailHeadInfo(title: String, artist: String, duration: Int, songCount: Int) {
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
                    Text(text = title, fontSize = 26.sp, lineHeight = 32.sp)
                    Text(text = artist, fontSize = 18.sp)
                    AlbumTimeAndTracksInfo(duration, songCount)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListItem(album: Album, song: Song, sameArtist: Boolean, addSong: (MediaItem) -> Unit) {
    Card(shape = RoundedCornerShape(0.dp), onClick = {
        addSong(ToMediaItem(song.id, album))
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