package np.ict.mad.mad25_p03_team03

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SyncedLyrics(
    rawLyrics: String,
    currentPosition: Long, // Passed from MusicProfile
    onSeek: (Long) -> Unit
) {
    //  Parse Lyrics
    val lyricList = remember(rawLyrics) {
        LyricsHelper.parseLyrics(rawLyrics)
    }

    // State for Scrolling
    val listState = rememberLazyListState()

    // Find Active Line Logic
    // We find the last line that started *before* the current time
    var activeIndex by remember { mutableStateOf(0) }

    LaunchedEffect(currentPosition) {
        // IndexOfLast finds the current line being played
        val index = lyricList.indexOfLast { it.timestamp <= currentPosition }

        if (index >= 0 && index != activeIndex) {
            activeIndex = index
            // Scroll to center the active line
            listState.animateScrollToItem(index, scrollOffset = -300)
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 200.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(lyricList) { index, line ->
            val isActive = (index == activeIndex)

            Text(
                text = line.content,
                color = if (isActive) Color.White else Color.Gray,
                fontSize = if (isActive) 24.sp else 18.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .clickable {
                        onSeek(line.timestamp)
                    }
            )
        }
    }
}