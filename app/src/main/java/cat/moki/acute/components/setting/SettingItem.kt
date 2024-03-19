package cat.moki.acute.components.setting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingGroup(title: String, content: @Composable () -> Unit = {}) {
    Column(modifier = Modifier.padding(top = 4.dp)) {
        ListItem(headlineContent = { Text(title) })
        Column {
            content()
        }
        Divider()
    }


}