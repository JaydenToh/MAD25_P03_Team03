package np.ict.mad.mad25_p03_team03

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colour palette
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)

@Composable
fun IdentifierHistoryScreen(
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onIdentifierClick: () -> Unit,
    onMoodPlaylistClick: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        IdentifiedSongHistory.ensureLoaded(context)
    }

    val historyItems = IdentifiedSongHistory.items

    // UI colors
    val primaryText = Color.White
    val secondaryText = Color.White.copy(alpha = 0.78f)
    val mutedText = Color.White.copy(alpha = 0.62f)
    val outline = Color.White.copy(alpha = 0.12f)

    val tabSelected = Color.White.copy(alpha = 0.16f)
    val tabUnselectedText = Color.White.copy(alpha = 0.72f)
    val tabContainer = CardColor1.copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground1)
            .padding(start = 16.dp, end = 16.dp, top = 25.dp, bottom = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = primaryText
                    )
                }
                Text(
                    text = "Identifier History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // segmented tab bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(
                        color = tabContainer,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = tabSelected, shape = RoundedCornerShape(14.dp))
                            .clickable { onHistoryClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "History",
                            fontSize = 13.sp,
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = Color.Transparent, shape = RoundedCornerShape(14.dp))
                            .clickable { onIdentifierClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Identifier",
                            fontSize = 13.sp,
                            color = tabUnselectedText,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = Color.Transparent, shape = RoundedCornerShape(14.dp))
                            .clickable { onMoodPlaylistClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Playlist",
                            fontSize = 13.sp,
                            color = tabUnselectedText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // List of identified song history
            if (historyItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs identified yet.\nTry using the Song Identifier first.",
                        fontSize = 16.sp,
                        color = secondaryText,
                        lineHeight = 20.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    itemsIndexed(historyItems) { index, item ->
                        HistoryItemRow(
                            item = item,
                            onMoodChange = { mood ->
                                IdentifiedSongHistory.updateMoodAt(index, mood)
                                IdentifiedSongHistory.saveToPreferences(context)
                            },
                            onDelete = {
                                IdentifiedSongHistory.deleteAt(index)
                                IdentifiedSongHistory.saveToPreferences(context)
                            },
                            cardColor = CardColor1,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            mutedText = mutedText,
                            outline = outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemRow(
    item: IdentifiedSongHistory.Item,
    onMoodChange: (String) -> Unit,
    onDelete: () -> Unit,
    cardColor: Color,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    outline: Color
) {
    var showMoodOptions by remember { mutableStateOf(false) }

    val iconBg = Color.White.copy(alpha = 0.08f)
    val pillSelected = Color.White.copy(alpha = 0.16f)
    val pillUnselected = Color.White.copy(alpha = 0.08f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        cardColor,
                        cardColor.copy(alpha = 0.82f)
                    )
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {

                    Text(
                        text = item.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.padding(top = 2.dp))

                    Text(
                        text = item.artist,
                        fontSize = 14.sp,
                        color = secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.padding(top = 6.dp))

                    Text(
                        text = item.timestamp,
                        fontSize = 12.sp,
                        color = mutedText
                    )

                    Spacer(modifier = Modifier.padding(top = 8.dp))

                    val moodLabel = item.mood ?: "No mood tagged"
                    Text(
                        text = "Mood: $moodLabel",
                        fontSize = 14.sp,
                        color = if (item.mood == null) mutedText else primaryText,
                        fontWeight = FontWeight.Medium
                    )
                }

                // icon stacked verticall
                Column(horizontalAlignment = Alignment.End) {

                    Box(
                        modifier = Modifier
                            .background(iconBg, RoundedCornerShape(12.dp))
                    ) {
                        IconButton(onClick = { showMoodOptions = !showMoodOptions }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit mood",
                                tint = primaryText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(top = 6.dp))

                    Box(
                        modifier = Modifier
                            .background(iconBg, RoundedCornerShape(12.dp))
                    ) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = primaryText
                            )
                        }
                    }
                }
            }

            if (showMoodOptions) {
                Spacer(modifier = Modifier.padding(top = 10.dp))

                val moods = listOf("Chill", "Hype", "Emotional")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moods.forEach { mood ->
                        Button(
                            onClick = {
                                onMoodChange(mood)
                                showMoodOptions = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (item.mood == mood) pillSelected else pillUnselected
                            ),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = mood,
                                color = primaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(top = 10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(outline, RoundedCornerShape(999.dp))
                        .padding(vertical = 0.5.dp)
                )
            }
        }
    }
}
