package cat.moki.acute.components.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cat.moki.acute.viewModels.PlayerViewModel

@Composable
fun RoundProgressButton(
    modifier: Modifier = Modifier,
    value: Float = 0f,
    button: @Composable () -> Unit
) {


    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        button()
        // background
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier
                .align(Alignment.Center)
                .aspectRatio(1f)
                .then(modifier),
            color = MaterialTheme.colorScheme.background,
        )
        CircularProgressIndicator(
            progress = value,
            modifier = Modifier
                .align(Alignment.Center)
                .then(modifier)
                .aspectRatio(1f),
        )


    }
}