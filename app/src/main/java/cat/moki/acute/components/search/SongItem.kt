package cat.moki.acute.components.search

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.common.MediaItem
import androidx.room.util.query
import cat.moki.acute.components.utils.SearchResultText
import cat.moki.acute.models.duration
import cat.moki.acute.utils.formatMS


@Composable
fun SongItem(mediaItem: MediaItem, query: String, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        overlineContent = { Text(mediaItem.mediaMetadata.albumTitle.toString()) },
        headlineContent = { SearchResultText(mediaItem.mediaMetadata.title.toString(), highlightText = query) },
        supportingContent = { Text(mediaItem.mediaMetadata.artist.toString()) },
        trailingContent = { Text(mediaItem.duration.formatMS()) }
    )
}