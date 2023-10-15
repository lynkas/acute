package cat.moki.acute

import android.content.ComponentName
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import cat.moki.acute.models.Album
import cat.moki.acute.models.Song
import cat.moki.acute.client.NetClient
import cat.moki.acute.ui.theme.AcuteTheme
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

class SinglePlayer : ComponentActivity() {
    private lateinit var album: Album
    private lateinit var song: Song
    private var mBound: Boolean = false
    private var player: MediaController? = null
    private lateinit var sessionToken: SessionToken

    //    private lateinit var connector: MediaSessionConnector
    private lateinit var controller: MediaController
    private lateinit var controllerFuture: ListenableFuture<MediaController>


    private fun initializeController() {
        controllerFuture =
            MediaController.Builder(
                this,
                SessionToken(this, ComponentName(this, PlayerService::class.java))
            )
                .buildAsync()
        controllerFuture.addListener({
            val controller = this.controller
            player = controller
        }, MoreExecutors.directExecutor())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        album = intent.getParcelableExtra("album")!!
        song = intent.getParcelableExtra("song")!!
//        player = ExoPlayer.Builder(this).build()
        setContent {
            AcuteTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlayerPage(album = album, song = song, player = player)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    override fun onStop() {
        super.onStop()
        player = null
        releaseController()
    }

    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlayerPage(album: Album, song: Song, player: MediaController?) {

    ConstraintLayout {
        val (cover, text, controlPanel) = createRefs()
        Box(modifier = Modifier
            .constrainAs(cover) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
            .fillMaxWidth()
            .padding(top = 64.dp)
        ) {
            Card(modifier = Modifier.align(Alignment.Center)) {
                GlideImage(
                    model = album.coverArt?.let { NetClient.getCoverArtUrl(it) },
                    contentDescription = "aaaa",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .aspectRatio(1f)
                )
            }

        }
        Box(modifier = Modifier.constrainAs(text) {
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            top.linkTo(cover.bottom)
            bottom.linkTo(controlPanel.top)
        }) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                album.title?.let {
//                    Text(
//                        text = it,
//                        fontSize = 20.sp,
//                        modifier = modifier.alpha(0.6f)
//                    )
//                }
                Text(
                    text = song.title,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    fontSize = 18.sp,
                    modifier = Modifier.alpha(0.6f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

            }

        }
        Box(modifier = Modifier
            .constrainAs(controlPanel) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)

            }
            .height(256.dp)
            .padding(horizontal = 24.dp)
        ) {

//            if (player != null) {
//                AndroidView(factory = { context ->
//
//                    (LayoutInflater.from(context).inflate(
//                        R.layout.player_control_view,
//                        null,
//                        false
//                    ) as PlayerControlView).apply {
//                        this.player = player
//
//                    }
////                    PlayerControlView(
////                        context,
////
////                    ).apply {
////                        showTimeoutMs = 0
////                    }
//                })
//            }

//            Column() {
//                Slider(value = 0.10f, onValueChange = {})
//                Box(modifier = Modifier.fillMaxWidth()) {
//                    Row(
//                        modifier = Modifier
//                            .align(Alignment.Center)
//                            .fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        IconButton(
//                            onClick = { /*TODO*/ }, modifier = Modifier.size(64.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.SkipPrevious,
//                                contentDescription = "", modifier = Modifier.size(64.dp)
//                            )
//                        }
//                        IconButton(onClick = { /*TODO*/ }, modifier = Modifier.size(96.dp)) {
//                            Icon(
//                                imageVector = Icons.Filled.PlayArrow,
//                                contentDescription = "",
//                                modifier = Modifier.size(96.dp)
//                            )
//
//                        }
//                        IconButton(
//                            onClick = { /*TODO*/ }, modifier = Modifier.size(64.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Filled.SkipNext,
//                                contentDescription = "", modifier = Modifier.size(64.dp)
//                            )
//                        }
//
//                    }
//                }
//
//            }
        }
//        ControlPanel(modifier = Modifier.constrainAs(controlPanel) {
//            top.linkTo(text.bottom)
//            start.linkTo(parent.start)
//            end.linkTo(parent.end)
//            bottom.linkTo(parent.bottom)
//
//        })
//        Button(
//            onClick = { /* Do something */ },
//            // Assign reference "button" to the Button composable
//            // and constrain it to the top of the ConstraintLayout
//            modifier = Modifier.constrainAs(button) {
//                top.linkTo(parent.top, margin = 16.dp)
//            }
//        ) {
//            Text("Button")
//        }

        // Assign reference "text" to the Text composable
        // and constrain it to the bottom of the Button composable
//        Text("Text", Modifier.constrainAs(text) {
//            top.linkTo(button.bottom, margin = 16.dp)
//        })
    }

}

@Composable
fun ControlPanel(modifier: Modifier) {
    Box(modifier = modifier.height(400.dp)) {
        Slider(value = 0.10f, onValueChange = {})

    }
}
