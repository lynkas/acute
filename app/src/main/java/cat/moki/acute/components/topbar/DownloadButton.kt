package cat.moki.acute.components.topbar

import androidx.annotation.OptIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.media3.common.util.UnstableApi
import cat.moki.acute.AcuteApplication
import cat.moki.acute.components.DownloadViewModelLocal
import cat.moki.acute.components.NavControllerLocal
import cat.moki.acute.routes.Strings
import cat.moki.acute.viewModels.DownloadEvent
import kotlinx.coroutines.launch

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@OptIn(UnstableApi::class)
@Composable
fun DownloadButton() {
    val navController = NavControllerLocal.current
    val scope = rememberCoroutineScope()
    val downloadViewModel = DownloadViewModelLocal.current
    var downloadNumber by rememberSaveable { mutableStateOf(AcuteApplication.application.downloadManager.currentDownloads.size) }

    IconButton(onClick = { navController.navigate(Strings.Download) }) {
        Icon(
            imageVector = Icons.Outlined.Download,
            contentDescription = "Localized description"
        )
        LaunchedEffect(true) {
            scope.launch {
                downloadViewModel.flow.collect {
                    when (it) {
                        is DownloadEvent.Downloads -> downloadNumber = it.list.size
                        else -> {}
                    }
                }
            }
        }
        if (downloadNumber != 0)
            Badge {
                Text("$downloadNumber")
            }

    }
}