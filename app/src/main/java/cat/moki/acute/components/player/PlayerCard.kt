package cat.moki.acute.components.player

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.moki.acute.components.PlayerViewModelLocal
import cat.moki.acute.components.utils.AutoCoverPic
import cat.moki.acute.viewModels.PlayerViewModel
import cat.moki.acute.models.Song
import cat.moki.acute.models.albumLocalMediaId
import cat.moki.acute.models.localMediaId

@Composable
fun PlayerCard(song: Song, operation: @Composable () -> Unit = {}) {
    val player = PlayerViewModelLocal.current
    Column {
        Row(
            modifier = Modifier
                .padding(bottom = 8.dp)
                .fillMaxWidth()
                .height(128.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .background(
                    Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.8f to Color.Transparent,
                            1f to MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )

        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .fillMaxHeight()
                    .padding(end = 8.dp),
            ) {
                val trackId = player.currentMediaItem.value?.localMediaId
                Log.d("TAGaaa", "PlayerCard: ${trackId}")
                Log.d("TAGaaa", "PlayerCard: ${player.currentMediaItem.value?.albumLocalMediaId}")
                AutoCoverPic(
                    trackId = song.localMediaId,
                    albumId = song.localAlbumMediaId
                )

            }

            Column(
                modifier = Modifier
                    .weight(2f)
                    .padding(bottom = 8.dp, top = 4.dp)
                    .fillMaxHeight()
            ) {
                song.album?.let { Text(text = it, fontSize = 12.sp, maxLines = 1) }
                Text(modifier = Modifier.weight(1f), text = song.title, fontSize = 20.sp, maxLines = 2)
                song.artist?.let { Text(text = it, fontSize = 16.sp, maxLines = 1) }
            }
        }
        operation()
    }

}