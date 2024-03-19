package cat.moki.acute.components.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.media3.common.MediaMetadata

enum class TabItem() {
    General, Albums, Songs, Artists;

    val icon: ImageVector
        get() = when (this) {
            General -> Icons.Outlined.Search
            Albums -> Icons.Outlined.Album
            Songs -> Icons.Outlined.Audiotrack
            Artists -> Icons.Outlined.People
        }

    val type: Int?
        get() = when (this) {
            General -> null
            Albums -> MediaMetadata.MEDIA_TYPE_ALBUM
            Songs -> MediaMetadata.MEDIA_TYPE_MUSIC
            Artists -> MediaMetadata.MEDIA_TYPE_ARTIST
        }
}

@Composable
fun Tabs(tabState: MutableState<TabItem>) {
    TabRow(
        selectedTabIndex = TabItem.entries.indexOf(tabState.value),
        tabs = {
            TabItem.entries.map {
                Tab(
                    selected = tabState.value == it,
                    onClick = { tabState.value = it },
                    text = { Text(it.name) },
                    icon = { Icon(it.icon, it.name) }
                )
            }
        }
    )
}