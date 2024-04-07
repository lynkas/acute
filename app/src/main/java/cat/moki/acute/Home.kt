package cat.moki.acute

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.SessionToken
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cat.moki.acute.components.DownloadViewModelLocal
import cat.moki.acute.components.LibraryViewModelLocal
import cat.moki.acute.components.NavControllerLocal
import cat.moki.acute.components.PlayerViewModelLocal
import cat.moki.acute.components.album.AlbumDetailComponent
import cat.moki.acute.components.bottomNav.NavBar
import cat.moki.acute.components.download.DownloadPage
import cat.moki.acute.components.library.Library
import cat.moki.acute.components.player.PlayList
import cat.moki.acute.components.playlist.LocalPlaylistDetail
import cat.moki.acute.components.playlist.Playlist
import cat.moki.acute.components.playlist.PlaylistDetail
import cat.moki.acute.components.search.Search
import cat.moki.acute.components.setting.AudioFilesList
import cat.moki.acute.components.setting.Setting
import cat.moki.acute.components.topbar.DownloadButton
import cat.moki.acute.components.topbar.SearchButton
import cat.moki.acute.models.toMediaId
import cat.moki.acute.routes.Strings
import cat.moki.acute.services.DataOfflineService
import cat.moki.acute.services.PlayerService
import cat.moki.acute.ui.theme.AcuteTheme
import cat.moki.acute.viewModels.DownloadViewModel
import cat.moki.acute.viewModels.LibraryViewModel
import cat.moki.acute.viewModels.PlayerViewModel


class Home : ComponentActivity() {
    private val download: DownloadViewModel by viewModels()
    private val library: LibraryViewModel by viewModels()
    private val player: PlayerViewModel by viewModels()
    private var browser: MediaBrowser? = null
    private var dataOfflineService: DataOfflineService? = null
    private var dataOfflineBinder: DataOfflineService.DataOfflineBinder? = null
    private val dataOfflineServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            dataOfflineService = (service as DataOfflineService.DataOfflineBinder).getService()
            dataOfflineBinder = service
            library.dataOfflineBinder = dataOfflineBinder
            Log.d(TAG, "onServiceConnected: dataOfflineServiceConnection connected ${service}")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            dataOfflineService = null
        }
    }
    private var shouldUnbindDataOfflineService = false

    fun bindDataOfflineService(): DataOfflineService.DataOfflineBinder? {
        // Attempts to establish a connection with the service.  We use an
        // explicit class name because we want a specific service
        // implementation that we know will be running in our own process
        // (and thus won't be supporting component replacement by other
        // applications).
        if (shouldUnbindDataOfflineService) return dataOfflineBinder
        if (bindService(
                Intent(this, DataOfflineService::class.java), dataOfflineServiceConnection, BIND_AUTO_CREATE
            )
        ) {
            shouldUnbindDataOfflineService = true
        } else {
            Log.e(
                "MY_APP_TAG", "Error: The requested service doesn't " +
                        "exist, or this client isn't allowed access to it."
            )
        }

        return dataOfflineBinder
    }

    fun unbindDataOfflineService() {
        if (shouldUnbindDataOfflineService) {
            // Release information about the service's state.
            unbindService(dataOfflineServiceConnection)
            shouldUnbindDataOfflineService = false
        }
    }


//    private val dataOfflineConnection = object : ServiceConnection {
//
//        override fun onServiceConnected(className: ComponentName, service: IBinder) {
//            // We've bound to LocalService, cast the IBinder and get LocalService instance.
//            val binder = service as ICacheServer.Stub
//            dataOfflineService = binder
//            mBound = true
//        }
//
//        override fun onServiceDisconnected(arg0: ComponentName) {
//            mBound = false
//        }
//    }

    val TAG = this::class.java.name


    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        dataOfflineBinder = bindDataOfflineService()
    }


    @androidx.annotation.OptIn(UnstableApi::class)
    private fun initializeBrowser() {
        val service = ComponentName(this, PlayerService::class.java)
        val browserFuture =
            MediaBrowser.Builder(
                this,
                SessionToken(this, service)
            ).setListener(object : MediaBrowser.Listener {
                override fun onSearchResultChanged(browser: MediaBrowser, query: String, itemCount: Int, params: MediaLibraryService.LibraryParams?) {
                    library.searchResultTime.value = System.currentTimeMillis()
                }
            })
                .buildAsync()
        browserFuture.addListener({
            browser = browserFuture.get()
            player.setBrowser(browserFuture.get())
            library.setBrowser(browserFuture.get())
        }, ContextCompat.getMainExecutor(this))

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        setContent {
            val navController = rememberNavController()
            CompositionLocalProvider(NavControllerLocal provides navController) {
                CompositionLocalProvider(PlayerViewModelLocal provides player) {
                    CompositionLocalProvider(LibraryViewModelLocal provides library) {
                        CompositionLocalProvider(DownloadViewModelLocal provides download) {
                            Content()
                        }
                    }
                }

            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        var showBottomSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        val downloadViewModel = DownloadViewModelLocal.current

        val navController = NavControllerLocal.current
        val currentRoute = navController
            .currentBackStackEntryFlow
            .collectAsState(initial = navController.currentBackStackEntry).value?.destination?.route
        if (!player.inited.value) return

        AcuteTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        Log.d(TAG, "Contenat: ${currentRoute}")
                        val route = navController.currentDestination?.route
                        if (currentRoute != Strings.Search)
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                                navigationIcon = {
                                    if (route?.startsWith(Strings.Main) == true)

                                        IconButton(onClick = { navController.navigate(Strings.Setting) }) {
                                            Icon(
                                                imageVector = Icons.Filled.Menu,
                                                contentDescription = "Localized description"
                                            )
                                        }
                                },
                                title = {},
                                actions = {
                                    if (route?.startsWith(Strings.Main) == true) {
                                        SearchButton()
                                        DownloadButton()
                                    }
                                }
                            )
                    },
                    bottomBar = {
                        if (currentRoute?.startsWith(Strings.Setting) == false)
                            Column {
                                NavBar(
                                    route = currentRoute,
                                    to = {
                                        navController.navigate(it) { popUpTo(0) }
                                    }) {
                                    Log.d(TAG, "Content: showBottomSheet ${showBottomSheet}")
                                    showBottomSheet = true
                                }
                            }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxWidth()
                            .fillMaxHeight()
                    ) {
                        NavHost(navController = navController, startDestination = Strings.Library) {
                            composable(Strings.Library) { Library() }
                            composable(Strings.Album) { backStackEntry ->
                                Log.d(TAG, "Content: ${backStackEntry.arguments?.getString(Strings.AlbumId)!!}")
                                AlbumDetailComponent(albumId = backStackEntry.arguments?.getString(Strings.AlbumId)!!.toMediaId())
                            }
                            composable(Strings.Setting) { Setting() }
                            composable(Strings.SettingFiles) { AudioFilesList() }
                            composable(Strings.Playlist) { Playlist() }
                            composable(Strings.PlaylistDetail) { backStackEntry ->
                                val id = backStackEntry.arguments?.getString(Strings.Id)!!.toMediaId()
                                if (id.serverId == "local") {
                                    LocalPlaylistDetail()
                                } else {
                                    PlaylistDetail(id)
                                }
                            }
                            composable(Strings.Search) { Search() }
                            composable(Strings.Download) { DownloadPage() }
                        }
                    }

                    if (showBottomSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showBottomSheet = false }, sheetState = sheetState
                        ) {
                            // Sheet content
                            PlayList()
//                            Button(onClick = {
//                                scope.launch { sheetState.hide() }.invokeOnCompletion {
//                                    if (!sheetState.isVisible) {
//                                        showBottomSheet = false
//                                    }
//                                }
//                            }) {
//                                Text("Hide bottom sheet")
//                            }
                        }
                    }

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initializeBrowser()
    }

    override fun onPause() {
        super.onPause()
        browser?.release()
        player.cleanTimeJob()
    }


    override fun onDestroy() {
        super.onDestroy()
        unbindDataOfflineService()

    }
}
