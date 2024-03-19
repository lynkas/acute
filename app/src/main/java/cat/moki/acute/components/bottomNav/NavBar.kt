package cat.moki.acute.components.bottomNav

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.outlined.Expand
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.unit.dp
import cat.moki.acute.routes.Strings

enum class NavBarPage(val s: String) {
    Album(Strings.Library),
    Playlists(Strings.Playlist), ;

    val displayName = s.replaceFirstChar { it.uppercase().split("/").first() }
    val defaultIcon: ImageVector
        get() {
            return when (this) {
                Album -> Icons.Outlined.LibraryMusic
                Playlists -> Icons.Outlined.QueueMusic
            }
        }
    val selectedIcon: ImageVector
        get() {
            return when (this) {
                Album -> Icons.Filled.LibraryMusic
                Playlists -> Icons.Filled.QueueMusic
            }
        }

}

@Composable
fun NavBar(route: String?, to: (route: String) -> Unit, clickPlaying: () -> Unit = {}) {

    val items = NavBarPage.entries.toList()

    NavigationBar {
        items.forEachIndexed { index, item ->
            val selected = route?.startsWith(item.s) ?: false
            NavigationBarItem(
                icon = { Icon(if (selected) item.selectedIcon else item.defaultIcon, contentDescription = item.displayName) },
                label = { Text(item.displayName) },
                selected = selected,
                onClick = { to(item.s) }
            )
        }
        NavigationBarItem(
            icon = { Icon((Icons.Outlined.Expand), contentDescription = "Play") },
            label = { Text("Playing") },
            selected = false,
            onClick = clickPlaying
        )


    }
}