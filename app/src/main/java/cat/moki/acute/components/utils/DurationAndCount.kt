package cat.moki.acute.components.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cat.moki.acute.utils.formatMS
import cat.moki.acute.utils.formatSecond

@Composable
fun DurationAndCount(time: Long, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        val modifier = Modifier.size(18.dp)
        Icon(Icons.Outlined.Timer, "time", modifier = modifier)
        Text(text = time.formatMS())
        Spacer(modifier = Modifier.width(6.dp))
        Icon(Icons.Outlined.MusicNote, "songs", modifier = modifier)
        Text(text = count.toString())

    }
}