package cat.moki.acute.components.setting

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NetworkLocked
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.NetClient
import cat.moki.acute.components.LibraryViewModelLocal
import cat.moki.acute.components.login.Login
import cat.moki.acute.models.Credential
import cat.moki.acute.models.RawCredential
import cat.moki.acute.models.ServerConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal enum class ServerStatus {
    OK, NotTested, Testing, Failed
}

internal enum class RescanStatus { Scanning, Done, Error, Init }

@Composable
fun ServerItem(credential: Credential) {
    val scope = rememberCoroutineScope()
    val library = LibraryViewModelLocal.current
    var testStatus by rememberSaveable { mutableStateOf(ServerStatus.NotTested) }
    var menuExpand by rememberSaveable { mutableStateOf(false) }
    var scanStatus by rememberSaveable { mutableStateOf(RescanStatus.Init) }
    var serverEditOpen by remember { mutableStateOf(false) }
    var serverEditTmp by remember { mutableStateOf(RawCredential()) }
    var serverConfig by remember {
        mutableStateOf(
            AcuteApplication.application.storage.serverConfiguration.get(credential.id) ?: ServerConfiguration()
        )
    }


    val TAG = "ServerItem"

//    fun statusAssign(result: Response<Res>, serverId: String) {
//        if (result.isSuccessful && result.body()!!.ok) {
//            val scanning = result.body()?.subsonicResponse?.scanStatus?.scanning
//            Log.d(TAG, "statusAssign: ${scanning}")
//            val previous = serverRescan[serverId]
//            serverRescan[serverId] = if (scanning == true) {
//                RescanStatus.Scanning
//            } else {
//                if (previous == RescanStatus.Scanning) {
//                    RescanStatus.Done
//                } else {
//                    RescanStatus.Init
//                }
//            }
//        } else {
//            serverRescan[serverId] = RescanStatus.Error
//        }
//    }
//
//    fun triggerScan(serverId: String) {
//        serverRescan[serverId] = RescanStatus.Scanning
//        coroutineScope.launch(Dispatchers.IO) {
//            val result = NetClient.request(serverId).startScan()
//            statusAssign(result, serverId)
//        }
//    }
//
//    suspend fun getScan(serverId: String) {
//        val result = NetClient.request(serverId).getScanStatus()
//        statusAssign(result, serverId)
//    }
//
//    suspend fun alwaysGetScan(serverId: String) {
//        delay(5000)
//        Log.d(TAG, "alwaysGetScan: serverRescan[serverId] == RescanStatus.Scanning ${serverRescan[serverId] == RescanStatus.Scanning}")
//        if (serverRescan[serverId] == RescanStatus.Scanning) getScan(serverId)
//        Log.d(TAG, "alwaysGetScan: serverRescan[serverId] == RescanStatus.Scanning ${serverRescan[serverId] == RescanStatus.Scanning}")
//        if (serverRescan[serverId] == RescanStatus.Scanning) return alwaysGetScan(serverId)
//    }
//
//    LaunchedEffect(true) {
//        AcuteApplication.application.servers.forEach {
//            getScan(it.id)
//        }
//    }
    LaunchedEffect(true) {
        if (!AcuteApplication.application.useOnlineSource) {
            testStatus = ServerStatus.NotTested
            return@LaunchedEffect
        }
        testStatus = ServerStatus.Testing
        scope.launch(Dispatchers.IO) {
            val result = NetClient.request(credential.id).ping().body()?.ok
//            scope.launch(Dispatchers.Main) {
//
//            }
            testStatus = when (result) {
                true -> ServerStatus.OK
                else -> ServerStatus.Failed
            }
        }
    }

    ListItem(
        modifier = Modifier.clickable {
            menuExpand = !menuExpand
        },
        headlineContent = {
            Text(
                credential.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            if (menuExpand) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    ListItem(
                        modifier = Modifier.clickable { serverEditOpen = true },
                        leadingContent = { Icon(Icons.Outlined.Edit, contentDescription = "") },
                        headlineContent = { Text("Edit") }
                    )
                    ListItem(
                        modifier = Modifier.clickable {
                            library.dataOfflineBinder?.cacheOperation("start", credential.id) ?: run {
                                Log.w(
                                    TAG,
                                    "ServerItem: no dataOfflineBinder",
                                )
                            }
                        },
                        leadingContent = { Icon(Icons.Outlined.CloudSync, contentDescription = "") },
                        headlineContent = { Text("Sync all data to local") }

                    )

                    fun changeInternetStatus(status: Boolean) {
                        serverConfig = serverConfig.copy(onlyUseLocalMetaData = status)
                        AcuteApplication.application.storage.updateServerConfiguration(credential.id, serverConfig)
                    }

                    ListItem(
                        modifier = Modifier.clickable { changeInternetStatus(!serverConfig.onlyUseLocalMetaData) },
                        leadingContent = { Icon(Icons.Outlined.NetworkLocked, contentDescription = "") },
                        headlineContent = { Text("Only use local metadata") },
                        supportingContent = { Text("albums, songs, playlists and covers information. not control audio data.") },
                        trailingContent = {
                            Switch(checked = serverConfig.onlyUseLocalMetaData, onCheckedChange = { changeInternetStatus(it) })
                        }
                    )
                    Divider()
                    ListItem(
                        leadingContent = { Icon(Icons.Outlined.DeleteOutline, contentDescription = "") },
                        headlineContent = { Text("Delete server info") },
                    )

                }
            }
        },
    )

    if (serverEditOpen) {
        LaunchedEffect(true) {
            serverEditTmp = RawCredential(
                credential.server,
                credential.username
            )
        }

        Dialog(
            onDismissRequest = { serverEditOpen = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

                ) {
                Login(
                    server = serverEditTmp.server,
                    username = serverEditTmp.username,
                    password = serverEditTmp.password
                )
            }
        }
    }
}