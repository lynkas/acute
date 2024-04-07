package cat.moki.acute.components.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.moki.acute.components.utils.AutoCoverPic
import cat.moki.acute.viewModels.PlayerViewModel
import cat.moki.acute.models.Song
import cat.moki.acute.models.toMediaId

data class PlayerControllerData(
    var song: Song? = null,
    var songId: Int = 0,
    var currentPosition: Long = 0,
    var bufferPosition: Long = 0,
    var isPlaying: Boolean = false,
    var isLoading: Boolean = false,
    var positionStart: Long = 0,
    var timeStart: Long = 0,
)
//
//@Composable
//fun PlayerController(player: PlayerViewModel) {
//    ListItem(
//        headlineContent = {
//            Text(
//                text = player.currentMediaMetadata.value?.title.toString(),
//                fontSize = 18.sp,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//            )
//        },
//        leadingContent = {
//            AutoCoverPic(
//                trackId = player.currentMediaItem.value?.mediaId?.toMediaId(),
//                albumId = player.currentMediaLocalItem.value?.id?.toMediaId()
//            )
//        },
//        supportingContent = {
//            Text(
//                text = player.currentMediaLocalItem.value?.artist.toString(),
//                fontSize = 14.sp,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//            )
//        },
//        trailingContent = {
//            RoundProgressButton(player = player)
//        }
//    )
//
//}

@Composable
fun RoundProgressButton(
    modifier: Modifier = Modifier,
    player: PlayerViewModel,
) {


    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        IconButton(
            modifier = modifier.align(Alignment.Center),
            onClick = { player.toggle() }

        ) {
            when (player.isPlaying.value) {
                true -> Icon(Icons.Filled.Pause, contentDescription = "")
                false -> Icon(Icons.Filled.PlayArrow, contentDescription = "")
            }
        }
        // background
        CircularProgressIndicator(
            100f,
            modifier = Modifier
                .align(Alignment.Center)
                .height(48.dp)
                .aspectRatio(1f),
            color = MaterialTheme.colorScheme.background,
        )
        // loading
        CircularProgressIndicator(
            player.loadingTime.longValue.toFloat() / player.totalTime.longValue.toFloat(),
            modifier = Modifier
                .align(Alignment.Center)
                .height(48.dp)
                .aspectRatio(1f),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.16f),
        )
        // playing

        if (player.isLoading.value && !player.isPlaying.value) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(48.dp)
                    .aspectRatio(1f),
            )
        } else {
            CircularProgressIndicator(
                player.playTime.toFloat() / (player.totalTime.longValue.toFloat()),
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(48.dp)
                    .aspectRatio(1f),
            )
        }

    }
}