package cat.moki.acute.components.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cat.moki.acute.client.LocalClient
import cat.moki.acute.client.NetClient
import cat.moki.acute.models.Credential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

enum class State {
    Success,
    Failed,
    NotTested,
    InternetError
}

@Composable
fun Login(
    server: String = "",
    username: String = "",
    password: String = "",
    finish: (suspend (credential: Credential) -> State)? = null
) {
    val server = rememberSaveable { mutableStateOf(server) }
    val username = rememberSaveable { mutableStateOf(username) }
    val password = rememberSaveable { mutableStateOf(password) }
    var status by rememberSaveable { mutableStateOf(State.NotTested) }
    val coroutineScope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .width(TextFieldDefaults.MinWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginInput(server, username, password)
        Text(status.name)

        Button(onClick = {
            val credential = Credential.make(
                if (server.value.endsWith("/")) {
                    server.value
                } else {
                    server.value + "/"
                }, username.value, password.value
            )
            finish?.let {
                coroutineScope.launch(Dispatchers.IO) {
                    status = finish(credential)
                }
            }

        }) {
            Text("Login")
        }
    }


}

@Composable
fun LoginInput(server: MutableState<String>, username: MutableState<String>, password: MutableState<String>) {
    val modifier = Modifier.padding(bottom = 4.dp)
    TextField(
        modifier = modifier,
        value = server.value,
        onValueChange = { server.value = it },
        label = { Text("Server") }
    )
    if (server.value.isNotEmpty() && !server.value.endsWith("/"))
        Text("Endpoint will be: ${server.value}/")
    TextField(
        modifier = modifier,
        value = username.value,
        onValueChange = { username.value = it },
        label = { Text("Username") }
    )
    TextField(
        modifier = modifier,
        value = password.value,
        onValueChange = { password.value = it },
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    )
}