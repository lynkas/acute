package cat.moki.acute


import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

val items = listOf("Songs", "Artists", "Playlists")


@Composable
fun Nav(navController: NavHostController) {
    var selectedItem by remember { mutableStateOf(0) }

    NavigationBar(modifier = Modifier.apply {
        heightIn(48.dp)
    }) {
        items.forEachIndexed { index, item ->
            this.NavigationBarItem(
                icon = { Icon(Icons.Filled.Favorite, contentDescription = item) },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(item)
                }
            )
        }

    }
}