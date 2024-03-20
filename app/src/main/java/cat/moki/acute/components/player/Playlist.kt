package cat.moki.acute.components.player

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import cat.moki.acute.components.PlayerViewModelLocal
import cat.moki.acute.components.utils.getVibrator
import cat.moki.acute.utils.formatSecond
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.NoDragCancelledAnimation
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.util.Collections
import kotlin.math.abs


fun readMediaList(browser: MediaBrowser): MutableList<MediaItem> {
    val list = mutableListOf<MediaItem>()
    for (i: Int in 0 until browser.mediaItemCount) {
        list.add(browser.getMediaItemAt(i))
    }
    return list
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayList(
) {
    val TAG = "PlayList"
    val context = LocalContext.current
    val player = PlayerViewModelLocal.current
    val scope = rememberCoroutineScope()
    val state = rememberReorderableLazyListState(
        onMove = { from, to -> { TODO() } },
        dragCancelledAnimation = NoDragCancelledAnimation()
    )
    if (player.playQueue.size == 0) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "¯\\_(ツ)_/¯", fontSize = 48.sp)
            Text(text = "")
            Spacer(modifier = Modifier.weight(1f))
        }
        return
    }
    val vibrator = getVibrator(context)
    val menuMode = remember { mutableStateOf<Int?>(null) }
    val editListMode = remember { mutableStateOf(false) }

    val shiftedList = remember {
        val list = player.playQueue.toMutableList()
        Collections.rotate(list, -(player.currentMediaIndex.value ?: 0) + 1)
        mutableStateListOf<MediaItem>().apply { addAll(list) }
    }
    val shiftedValue = remember { mutableIntStateOf(player.currentMediaIndex.value ?: 0) }

    fun firstTimeUpdateList() {
        val list = player.playQueue.toMutableList()
        shiftedValue.value = player.currentMediaIndex.value ?: 0
        Collections.rotate(list, -shiftedValue.value + 1)
        shiftedList.clear()
        shiftedList.addAll(list)

    }

    LaunchedEffect(true) {
        firstTimeUpdateList()
    }


    LazyColumn(
        state = state.listState,
        modifier = Modifier
            .reorderable(state)
            .fillMaxHeight()
    ) {


        @Composable
        fun SmallIcon(icon: ImageVector) {
            Icon(
                icon,
                modifier = Modifier.size(36.dp),
                contentDescription = ""
            )
        }

        val threshold = 0.9f
        itemsIndexed(shiftedList) { i, song ->
            val realIndex = (player.playQueue.size + i + shiftedValue.value - 1) % player.playQueue.size
            val isCurrentMedia = player.currentMediaIndex.value == realIndex
            val swipeMax = remember { mutableFloatStateOf(1000f) }
            val itemVisibility = remember {
                Animatable(1f)
            }

            fun listChangeUpdate(removeIndex: Int) {
                scope.launch {
                    Log.d(TAG, "listChangeUpdate: 1")
                    itemVisibility.animateTo(0f, tween(20))
                    Log.d(TAG, "listChangeUpdate: 2")
                    player.browser.value.removeMediaItem(removeIndex)
                    Log.d(TAG, "listChangeUpdate: $removeIndex ${shiftedValue.value}")
                    val delta = if (removeIndex < shiftedValue.value - 1) -1 else 0
                    val list = player.playQueue.toMutableList()
                    shiftedValue.value += delta
                    Collections.rotate(list, -shiftedValue.value + 1)
                    shiftedList.clear()
                    shiftedList.addAll(list)
                }
            }

//            val swipeToDismissBoxState = rememberSwipeToDismissBoxState(positionalThreshold = {
//                swipeMax.value = it
//                it * threshold
//            }, confirmValueChange = {
//                Log.d(TAG, "confirmValueChange: $it")
//                if (it != SwipeToDismissBoxValue.Settled) {
//                    listChangeUpdate(realIndex)
//                }
//                true
//            })


//            fun offsetPercentage(): Float {
//                val offset = try {
//                    abs(swipeToDismissBoxState.requireOffset())
//                } catch (e: Exception) {
//                    0f
//                }
//                return offset / swipeMax.floatValue
//            }

            @Composable
            fun ItemWrap(content: @Composable () -> Unit) {
                var columnHeightDp by remember {
                    mutableStateOf(0.dp)
                }
                Box {
                    content()

                }
//                if (editListMode.value) {
//                    SwipeToDismissBox(
//                        state = swipeToDismissBoxState,
//                        backgroundContent = {
//                            Box(
//                                modifier = Modifier
//                                    .background(
//                                        color = Color
//                                            .hsl(10f, 0.6f, 0.34f)
//                                            .copy(alpha = offsetPercentage())
//                                    )
//                                    .fillMaxWidth()
//                            )
//                        },
//                        enableDismissFromEndToStart = true
//                    ) {
//                        content()
//                    }
//                } else {
//                    Box {
//                        content()
//
//                    }
//
//                }
            }

            ItemWrap {
                if (isCurrentMedia) {
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp)
                        ) {
                            player.currentMediaLocalItem.value?.let {
                                PlayerCard(it) {
                                    Row(modifier = Modifier.padding(top = 4.dp)) {
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.CenterVertically)
                                                .width(IntrinsicSize.Min)
                                        ) {

                                            Text(
                                                text = (player.playTime.toInt() / 1000).formatSecond(),
                                                color = if (player.isMovingBar.value) {
                                                    MaterialTheme.colorScheme.secondary
                                                } else {
                                                    Color.Unspecified
                                                }
                                            )
                                            Divider()
                                            Text((player.totalTime.longValue.toInt() / 1000).formatSecond())
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Row {
                                            val openMenu = remember { mutableStateOf(false) }
                                            Box(
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            ) {
                                                IconButton(
                                                    modifier = Modifier
                                                        .size(36.dp),
                                                    onClick = { openMenu.value = true }

                                                ) {
                                                    Icon(
                                                        Icons.Outlined.MoreVert,
                                                        modifier = Modifier.size(24.dp),
                                                        contentDescription = ""
                                                    )
                                                }
                                                DropdownMenu(expanded = openMenu.value, onDismissRequest = { openMenu.value = false }) {
                                                    DropdownMenuItem(
                                                        text = { Text("Clear Playlist") },
                                                        onClick = {
                                                            player.browser.value.clearMediaItems()
                                                            firstTimeUpdateList()
                                                        },
                                                        leadingIcon = { Icon(Icons.Outlined.PlaylistRemove, contentDescription = "") })
                                                    DropdownMenuItem(
                                                        text = { Text(if (editListMode.value) "Exit Edit" else "Edit Playlist") },
                                                        onClick = {
                                                            editListMode.value = !editListMode.value
                                                        },
                                                        leadingIcon = { Icon(Icons.Outlined.EditNote, contentDescription = "") })
                                                }

                                            }
                                            IconButton(
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                                    .size(36.dp),
                                                onClick = {
                                                    player.browser.value.repeatMode = (player.browser.value.repeatMode + 1) % 3
                                                }

                                            ) {
                                                when (player.loop.value) {
                                                    Player.REPEAT_MODE_OFF -> Icon(
                                                        Icons.Outlined.Repeat,
                                                        modifier = Modifier.size(24.dp),
                                                        tint = LocalContentColor.current.copy(alpha = 0.4f),
                                                        contentDescription = ""
                                                    )

                                                    Player.REPEAT_MODE_ONE -> Icon(
                                                        Icons.Outlined.RepeatOne,
                                                        modifier = Modifier.size(24.dp),
                                                        contentDescription = ""
                                                    )

                                                    Player.REPEAT_MODE_ALL -> Icon(
                                                        Icons.Outlined.Repeat,
                                                        modifier = Modifier.size(24.dp),
                                                        contentDescription = ""
                                                    )

                                                }
                                            }
                                            IconButton(
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                                    .size(36.dp),
                                                onClick = { player.browser.value.shuffleModeEnabled = !player.browser.value.shuffleModeEnabled }

                                            ) {
                                                when (player.shuffle.value) {
                                                    true -> Icon(
                                                        Icons.Outlined.Shuffle,
                                                        modifier = Modifier.size(24.dp),
                                                        contentDescription = "",
                                                    )

                                                    false -> Icon(
                                                        Icons.Outlined.Shuffle,
                                                        modifier = Modifier.size(24.dp),
                                                        contentDescription = "",
                                                        tint = LocalContentColor.current.copy(alpha = 0.4f),
                                                    )
                                                }
                                            }
                                            IconButton(
                                                modifier = Modifier
                                                    .align(Alignment.CenterVertically)
                                                    .size(48.dp),
                                                onClick = { player.toggle() }

                                            ) {
                                                when (player.isPlaying.value) {
                                                    true -> Icon(Icons.Filled.Pause, modifier = Modifier.size(36.dp), contentDescription = "")
                                                    false -> Icon(Icons.Filled.PlayArrow, modifier = Modifier.size(36.dp), contentDescription = "")
                                                }
                                            }
                                        }

                                    }
                                    val send = remember { mutableStateOf(false) }

                                    LaunchedEffect(send.value) {
                                        if (send.value) {
                                            player.seek(time = player.currentTime.longValue)
                                            player.isMovingBar.value = false
                                            send.value = false
                                        }
                                    }


                                    Slider(
                                        onValueChange = {
                                            Log.d(TAG, "PlayList: player.isMovingBar.value ${player.isMovingBar.value}")
                                            if (!player.isMovingBar.value) {
                                                player.isMovingBar.value = true
                                            }
                                            player.currentTime.longValue = it.toLong()
                                        },
                                        onValueChangeFinished = {
                                            Log.d(TAG, "onValueChangeFinished")
                                            Log.d(TAG, "a: aaaa")
                                            send.value = true
                                        },
//                                        valueRange = 0f..100f,
                                        valueRange = 0f..player.totalTime.longValue.toFloat(),
                                        value = player.playTime.toFloat(),
//                                        value = time.floatValue
                                    )
                                }
                            }

                        }
                    }

                } else
                    PlaylistItem(song = song, realIndex, ::listChangeUpdate, menuMode.value == realIndex) { open ->
                        menuMode.value = if (open) realIndex else null
                    }
            }

            if (realIndex == player.playQueue.size - 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.Center)
                    )

                }
            }

        }

    }
}
