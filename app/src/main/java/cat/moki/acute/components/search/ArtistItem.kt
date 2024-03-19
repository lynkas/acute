package cat.moki.acute.components.search

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.media3.common.MediaItem
import androidx.room.util.query
import cat.moki.acute.components.utils.SearchResultText
import cat.moki.acute.models.duration
import cat.moki.acute.utils.formatMS


@Composable
fun ArtistItem(mediaItem: MediaItem, query: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { SearchResultText(mediaItem.mediaMetadata.title.toString(), highlightText = query) },
    )
}