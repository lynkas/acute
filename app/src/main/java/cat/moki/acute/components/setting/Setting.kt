package cat.moki.acute.components.setting

import android.app.DownloadManager
import android.content.Intent
import android.provider.DocumentsProvider
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import cat.moki.acute.AcuteApplication
import cat.moki.acute.client.NetClient
import cat.moki.acute.components.NavControllerLocal
import cat.moki.acute.components.login.Login
import cat.moki.acute.components.login.State
import cat.moki.acute.models.RawCredential
import cat.moki.acute.models.Res
import cat.moki.acute.routes.Strings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.File


enum class RescanStatus { Scanning, Done, Error, Init }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Setting() {
    val TAG = "Setting"
    val settings = remember { mutableStateOf(AcuteApplication.application.settings, neverEqualPolicy()) }
    val scope = rememberCoroutineScope()
    val serverTest = remember { mutableStateMapOf<String, Boolean>() }
    val serverRescan = remember { mutableStateMapOf<String, RescanStatus>() }

    val serverEditOpen = remember { mutableStateOf(false) }
    val serverEditTmp = remember { mutableStateOf(RawCredential()) }
    val serverEditId = remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val navController = NavControllerLocal.current

    fun update(content: () -> Unit) {
        content()
        settings.value = settings.value
    }

    val context = LocalContext.current
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        SettingGroup("General") {
            fun toggleWifi(value: Boolean) {
                update { settings.value.internetWifiOnly = value }
            }
            ListItem(
                modifier = Modifier.clickable { toggleWifi(!settings.value.internetWifiOnly) },
                headlineContent = { Text("Use Wifi Only") },
                trailingContent = {
                    Switch(
                        checked = settings.value.internetWifiOnly,
                        onCheckedChange = { toggleWifi(it) }
                    )
                },
            )
            fun toggleDup(value: Boolean) {
                update { settings.value.addDuplicateSongInPlaylist = value }
            }
            ListItem(
                modifier = Modifier.clickable { toggleDup(!settings.value.addDuplicateSongInPlaylist) },
                headlineContent = { Text("Allow duplicate song") },
                supportingContent = { Text("or directly play the added one") },
                trailingContent = {
                    Switch(
                        checked = settings.value.addDuplicateSongInPlaylist,
                        onCheckedChange = { toggleDup(it) }
                    )
                },
            )
            fun toggleSmallBracket(value: Boolean) {
                update { settings.value.smallEndBrackets = value }
            }

            ListItem(
                modifier = Modifier.clickable { toggleSmallBracket(!settings.value.smallEndBrackets) },
                headlineContent = { Text("smallEndBrackets") },
                trailingContent = {
                    Switch(
                        checked = settings.value.smallEndBrackets,
                        onCheckedChange = { toggleSmallBracket(it) }
                    )
                },
            )

            ListItem(
                modifier = Modifier.clickable {
                    navController.navigate(Strings.SettingFiles)
//                    val intent = Intent(Intent.ACTION_VIEW)
//                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                    val a = FileProvider.getUriForFile(context, context.packageName, File(AcuteApplication.application.audioCacheDir, "1.txt"))
//                    Log.d(TAG, "Setting: $a")
//
//                    intent.setDataAndType(
//                        a,
//                        "*/*"
//                    ) // or use */*
//
//                    startActivity(context, intent, null)
                },
                headlineContent = { Text("Audio Files Browser") },
            )

        }





        fun statusAssign(result: Response<Res>, serverId: String) {
            if (result.isSuccessful && result.body()!!.ok) {
                val scanning = result.body()?.subsonicResponse?.scanStatus?.scanning
                Log.d(TAG, "statusAssign: ${scanning}")
                val previous = serverRescan[serverId]
                serverRescan[serverId] = if (scanning == true) {
                    RescanStatus.Scanning
                } else {
                    if (previous == RescanStatus.Scanning) {
                        RescanStatus.Done
                    } else {
                        RescanStatus.Init
                    }
                }
            } else {
                serverRescan[serverId] = RescanStatus.Error
            }
        }

        fun triggerScan(serverId: String) {
            serverRescan[serverId] = RescanStatus.Scanning
            coroutineScope.launch(Dispatchers.IO) {
                val result = NetClient.request(serverId).startScan()
                statusAssign(result, serverId)
            }
        }

        suspend fun getScan(serverId: String) {
            val result = NetClient.request(serverId).getScanStatus()
            statusAssign(result, serverId)
        }

        suspend fun alwaysGetScan(serverId: String) {
            delay(5000)
            Log.d(TAG, "alwaysGetScan: serverRescan[serverId] == RescanStatus.Scanning ${serverRescan[serverId] == RescanStatus.Scanning}")
            if (serverRescan[serverId] == RescanStatus.Scanning) getScan(serverId)
            Log.d(TAG, "alwaysGetScan: serverRescan[serverId] == RescanStatus.Scanning ${serverRescan[serverId] == RescanStatus.Scanning}")
            if (serverRescan[serverId] == RescanStatus.Scanning) return alwaysGetScan(serverId)
        }

        LaunchedEffect(true) {
            AcuteApplication.application.servers.forEach {
                getScan(it.id)
            }
        }

        SettingGroup("Accounts") {
            Column {
                AcuteApplication.application.servers.forEach {
                    LaunchedEffect(true) {
                        scope.launch(Dispatchers.IO) {
                            serverTest[it.id] = NetClient.request(it).ping().body()?.subsonicResponse?.status == "ok"
                        }
                    }
                    ListItem(
                        modifier = Modifier.clickable {
                            serverEditOpen.value = true
                            serverEditTmp.value = RawCredential(
                                server = it.server,
                                username = it.username
                            )
                            serverEditId.value = it.id
                        },
                        headlineContent = {
                            Text(
                                it.displayName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        supportingContent = {
                            when (serverTest[it.id]) {
                                null -> Text("Testing")
                                true -> Text("Pass")
                                false -> Text("Error")
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                triggerScan(it.id)
                                scope.launch(Dispatchers.IO) { alwaysGetScan(it.id) }
                            }) {
                                when (serverRescan[it.id]) {
                                    null -> {}
                                    RescanStatus.Scanning -> CircularProgressIndicator()
                                    RescanStatus.Init -> Icon(Icons.Filled.Refresh, "Refresh")
                                    RescanStatus.Done -> Icon(Icons.Filled.Check, "Done")
                                    RescanStatus.Error -> Icon(Icons.Filled.Close, "Error")
                                }
                            }
                        }
                    )
                }

            }
            ListItem(
                modifier = Modifier.clickable {
                    serverEditOpen.value = true
                    serverEditTmp.value = RawCredential()
                    serverEditId.value = ""

                },
                leadingContent = { Icon(Icons.Filled.Add, "Add") },
                headlineContent = { Text("Add a server") },
            )
            if (serverEditOpen.value)
                Dialog(
                    onDismissRequest = { serverEditOpen.value = false }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        ) {
                        Login(
                            server = serverEditTmp.value.server,
                            username = serverEditTmp.value.username,
                            password = serverEditTmp.value.password
                        ) {
                            NetClient.request(it).ping().run {
                                return@Login body()?.run {
                                    if (!isSuccessful) {
                                        State.InternetError
                                    } else if (subsonicResponse.status == "ok") {
                                        AcuteApplication.application.run {
                                            addAccount(it)
                                            removeAccount(serverEditId.value)
                                            initServers()
                                        }
                                        State.Success
                                    } else {
                                        State.Failed
                                    }
                                } ?: run {
                                    State.InternetError
                                }
                            }
                        }
                    }
                }
        }
    }

}
