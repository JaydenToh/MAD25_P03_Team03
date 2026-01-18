package np.ict.mad.mad25_p03_team03

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class IdentifierHistory : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IdentifierHistoryScreen(
                onBackClick = { finish() }
            )
        }
    }
}

@Composable
fun IdentifierHistoryScreen(
    onBackClick: () -> Unit
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
            // 👇 same outer padding as SongIdentifier
            .padding(start = 16.dp, end = 16.dp, top = 60.dp, bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // ---------- Header ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
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

            // ---------- List area (takes remaining height above nav) ----------
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

            // ---------- Bottom Navigation (copied style from SongIdentifier) ----------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0x33000000),
                            shape = RoundedCornerShape(50.dp)   // same pill shape
                        )
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // History - this screen is active → blue
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { /* already on this screen */ },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "History",
                            tint = Color(0xFF4C6FFF),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "History",
                            fontSize = 12.sp,
                            color = Color(0xFF4C6FFF),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Song Identifier – go back
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onBackClick() },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Identifier",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Identifier",
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }

                    // Playlist
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                context.startActivity(
                                    Intent(context, MoodPlaylist::class.java)
                                )
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Playlist",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Playlist",
                            fontSize = 12.sp,
                            color = Color.White
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.artist,
                        fontSize = 14.sp,
                        color = Color(0xFFB0BEC5),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Identified on ${item.timestamp}",
                        fontSize = 12.sp,
                        color = Color(0x88FFFFFF)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mood: ${item.mood ?: "Not set"}",
                        fontSize = 14.sp,
                        color = Color(0xFFB3C3FF),
                        fontWeight = FontWeight.Medium
                    )
                }

                // Edit + Delete icons stacked vertically
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            contentDescription = "Delete entry",
                            tint = Color(0xFFFF5252)
                        )
                    }
                }
            }

            if (showMoodOptions) {
                Spacer(modifier = Modifier.height(8.dp))
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
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (item.mood == mood)
                                    Color(0xFF4C6FFF)
                                else
                                    Color(0x33FFFFFF)
                            ),
                            contentPadding = PaddingValues(
                                vertical = 6.dp,
                                horizontal = 4.dp
                            )
                        ) {
                            Text(
                                text = mood,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
