package cat.moki.acute.components

import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import cat.moki.acute.viewModels.DownloadViewModel
import cat.moki.acute.viewModels.LibraryViewModel
import cat.moki.acute.viewModels.PlayerViewModel

val PlayerViewModelLocal = compositionLocalOf<PlayerViewModel> { error("No player model") }
val LibraryViewModelLocal = compositionLocalOf<LibraryViewModel> { error("No library model") }
val DownloadViewModelLocal = compositionLocalOf<DownloadViewModel> { error("No download model") }
val NavControllerLocal = compositionLocalOf<NavHostController> { error("No nav host controller") }
