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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1C1B3A),
                        Color(0xFF311B92)
                    )
                )
            )
            .padding(start = 16.dp, end = 16.dp, top = 25.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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
                        tint = Color.White
                    )
                }
                Text(
                    text = "Identifier History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // segmented tab bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val selectedColor = Color(0xFF4C6FFF)
                    val unselectedText = Color(0xCCFFFFFF)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = selectedColor, shape = RoundedCornerShape(50.dp))
                            .clickable { onHistoryClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "History",
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = Color.Transparent, shape = RoundedCornerShape(50.dp))
                            .clickable { onIdentifierClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Identifier",
                            fontSize = 13.sp,
                            color = unselectedText,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = Color.Transparent, shape = RoundedCornerShape(50.dp))
                            .clickable { onMoodPlaylistClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Playlist",
                            fontSize = 13.sp,
                            color = unselectedText,
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
                        color = Color(0xCCFFFFFF),
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
                            }
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
    onDelete: () -> Unit
) {
    var showMoodOptions by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF211A3B),
                        Color(0xFF2B1F4F)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.padding(top = 2.dp))

                    Text(
                        text = item.artist,
                        fontSize = 14.sp,
                        color = Color(0xCCFFFFFF),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.padding(top = 6.dp))

                    Text(
                        text = item.timestamp,
                        fontSize = 12.sp,
                        color = Color(0x99FFFFFF)
                    )

                    Spacer(modifier = Modifier.padding(top = 8.dp))

                    val moodLabel = item.mood ?: "No mood tagged"
                    Text(
                        text = "Mood: $moodLabel",
                        fontSize = 14.sp,
                        color = if (item.mood == null) Color(0x99FFFFFF) else Color(0xFFB3E5FC),
                        fontWeight = FontWeight.Medium
                    )
                }

                // icon stacked vertically
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    IconButton(onClick = { showMoodOptions = !showMoodOptions }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit mood",
                            tint = Color(0xFFFFD54F)
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFFF6B6B)
                        )
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
                                containerColor = if (item.mood == mood) Color(0xFF4C6FFF) else Color(0x33000000)
                            ),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = mood,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
