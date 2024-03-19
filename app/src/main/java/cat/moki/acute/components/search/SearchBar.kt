package cat.moki.acute.components.search

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cat.moki.acute.components.NavControllerLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(query: MutableState<String>, search: () -> Unit = {}, content: @Composable () -> Unit) {
    val TAG = "SearchPage"
    val lastInputChange = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val currentTime = remember { mutableLongStateOf(System.currentTimeMillis()) }
    val searching = remember { mutableStateOf(false) }
    val navController = NavControllerLocal.current
    val job = remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val searchDelay = 500

    LaunchedEffect(query.value) {
        job.value?.cancel()
        job.value = scope.launch(Dispatchers.IO) {
            while (true) {
                currentTime.longValue = System.currentTimeMillis()
                delay(10)
                if (currentTime.longValue - lastInputChange.value > searchDelay) {
                    search()
                    return@launch
                }
            }
        }
    }
    val progressModifier = Modifier.fillMaxWidth()
    SearchBar(
        modifier = Modifier.fillMaxHeight(),
        onActiveChange = { if (!it) navController.popBackStack() },
        onQueryChange = {
            query.value = it
            lastInputChange.value = System.currentTimeMillis()
        },
        onSearch = {},
        query = query.value,
        active = true,
    ) {
        if (searching.value) LinearProgressIndicator(progressModifier)
        else LinearProgressIndicator(
            modifier = progressModifier,
            progress = { (currentTime.longValue - lastInputChange.value) / searchDelay.toFloat() })
        content()
    }


}