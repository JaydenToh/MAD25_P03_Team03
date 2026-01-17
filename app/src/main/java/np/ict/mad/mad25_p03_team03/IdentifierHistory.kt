package np.ict.mad.mad25_p03_team03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val items = IdentifiedSongHistory.items

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF59168B),
                        Color(0xFF312C85),
                        Color(0xFF1C398E)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Identifier History",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No songs identified yet.\nTry identifying a song first.",
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color(0xCCFFFFFF)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { songItem ->
                        HistoryItemCard(songItem = songItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(songItem: IdentifiedSongItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0x33000000),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = songItem.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Artist: ${songItem.artist}",
            fontSize = 14.sp,
            color = Color(0xCCFFFFFF)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Mood section
        val currentMood = songItem.mood

        if (currentMood != null) {
            // Mood already selected (either from SongIdentifier or here)
            Text(
                text = "Mood tagged as: $currentMood",
                fontSize = 14.sp,
                color = Color(0xFFB3E5FC),
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = "Pick a mood for this song:",
                fontSize = 14.sp,
                color = Color(0xCCFFFFFF)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val moods = listOf("Chill", "Hype", "Emotional")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                moods.forEach { mood ->
                    Button(
                        onClick = {
                            // only songs with null mood come here
                            IdentifiedSongHistory.updateMood(songItem.id, mood)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x33FFFFFF)
                        ),
                        contentPadding = PaddingValues(
                            vertical = 6.dp,
                            horizontal = 8.dp
                        )
                    ) {
                        Text(
                            text = mood,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
