package cat.moki.acute.components.topbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import cat.moki.acute.components.NavControllerLocal
import cat.moki.acute.routes.Strings

@Composable
fun SearchButton() {
    val navController = NavControllerLocal.current
    IconButton(onClick = { navController.navigate(Strings.Search) }) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Localized description"
        )
    }
}