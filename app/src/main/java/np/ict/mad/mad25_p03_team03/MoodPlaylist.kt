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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

data class MoodTrack(
    val title: String,
    val artist: String,
    val note: String
)

// Hardcoded playlists for each mood
private val chillTracks = listOf(
    MoodTrack("Sunset Drive", "Lofi Collective", "Soft beats perfect for unwinding."),
    MoodTrack("Raindrop Thoughts", "Dreamwave", "Relaxing background vibes."),
    MoodTrack("Quiet Nights", "Midnight Echo", "Calm, slow, and peaceful."),
    MoodTrack("Cloud Walker", "Skyline", "Chill track for late night focus.")
)

private val hypeTracks = listOf(
    MoodTrack("Adrenaline Rush", "Neon Pulse", "High-energy beat to get you moving."),
    MoodTrack("Level Up", "Game On", "Perfect for workouts or gaming sessions."),
    MoodTrack("Night Runner", "City Lights", "Fast tempo, driving rhythm."),
    MoodTrack("Turn It Up", "Bassline Crew", "Party anthem vibes.")
)

private val emotionalTracks = listOf(
    MoodTrack("Fading Memories", "Echo Heart", "Soft piano with emotional vocals."),
    MoodTrack("Almost Home", "Aurora Lane", "Melancholic but hopeful ballad."),
    MoodTrack("Broken Radio", "Silent Signal", "Emo indie style storytelling."),
    MoodTrack("Late Night Letters", "Paperplanes", "Slow, reflective, and heartfelt.")
)

// Colour palette
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)

@Composable
fun MoodPlaylistScreen(
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

    // Count moods from history
    val moodCounts = remember(historyItems) {
        val counts = mutableMapOf<String, Int>()
        historyItems.forEach { item ->
            val mood = item.mood
            if (!mood.isNullOrBlank()) {
                counts[mood] = (counts[mood] ?: 0) + 1
            }
        }
        counts
    }

    val dominantMood: String? = if (moodCounts.isEmpty()) {
        null
    } else {
        moodCounts.maxByOrNull { it.value }?.key
    }

    var overrideMood by remember { mutableStateOf<String?>(null) }
    val activeMood: String? = overrideMood ?: dominantMood

    // Base playlist based on the active mood
    val baseTracks: List<MoodTrack> = when (activeMood) {
        "Chill" -> chillTracks
        "Hype" -> hypeTracks
        "Emotional" -> emotionalTracks
        else -> emptyList()
    }

    // Shuffled playlist that changes when mood changes
    var displayedTracks by remember(activeMood) {
        mutableStateOf(
            if (baseTracks.isNotEmpty()) baseTracks.shuffled() else emptyList()
        )
    }

    // UI colors (white text)
    val primaryText = Color.White
    val secondaryText = Color.White.copy(alpha = 0.78f)
    val mutedText = Color.White.copy(alpha = 0.62f)
    val outline = Color.White.copy(alpha = 0.12f)
    val selectedPill = Color.White.copy(alpha = 0.14f)
    val unselectedPill = Color.White.copy(alpha = 0.08f)
    val tabSelected = Color.White.copy(alpha = 0.16f)
    val tabUnselectedText = Color.White.copy(alpha = 0.72f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground1)
            .padding(start = 16.dp, end = 16.dp, top = 25.dp, bottom = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top header
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
                    text = "Mood Playlist",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Segmented tab bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .background(
                        color = CardColor1.copy(alpha = 0.55f),
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
                            .background(color = Color.Transparent, shape = RoundedCornerShape(14.dp))
                            .clickable { onHistoryClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "History",
                            fontSize = 13.sp,
                            color = tabUnselectedText,
                            fontWeight = FontWeight.Medium
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
                            .background(color = tabSelected, shape = RoundedCornerShape(14.dp))
                            .clickable { onMoodPlaylistClick() }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Playlist",
                            fontSize = 13.sp,
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 4.dp))

            // Mood card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 190.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CardColor1,
                                CardColor1.copy(alpha = 0.78f)
                            )
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    if (activeMood == null) {
                        Text(
                            text = "No mood detected yet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.padding(top = 8.dp))
                        Text(
                            text = "Tag moods in your Identifier History to unlock a personalised playlist.",
                            fontSize = 15.sp,
                            color = secondaryText,
                            lineHeight = 20.sp
                        )
                    } else {
                        val headerLabel = if (overrideMood == null) {
                            "Your current vibe (Auto):"
                        } else {
                            "Current mood (Manual):"
                        }

                        Text(
                            text = headerLabel,
                            fontSize = 14.sp,
                            color = secondaryText
                        )
                        Spacer(modifier = Modifier.padding(top = 6.dp))
                        Text(
                            text = activeMood,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryText
                        )
                        Spacer(modifier = Modifier.padding(top = 10.dp))

                        if (overrideMood == null && moodCounts.isNotEmpty()) {
                            val totalTagged = moodCounts.values.sum()
                            Text(
                                text = "Based on $totalTagged tagged songs in your Identifier History.",
                                fontSize = 13.sp,
                                color = mutedText
                            )
                        } else if (overrideMood != null) {
                            Text(
                                text = "You manually selected this mood for your playlist.",
                                fontSize = 13.sp,
                                color = mutedText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.padding(top = 14.dp))

                    // Mood override pills
                    val moodsForOverride = listOf("Auto", "Chill", "Hype", "Emotional")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        moodsForOverride.forEach { moodLabel ->
                            val isSelected = when (moodLabel) {
                                "Auto" -> overrideMood == null
                                else -> overrideMood == moodLabel
                            }

                            val bgColor = if (isSelected) selectedPill else unselectedPill

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = bgColor,
                                        shape = RoundedCornerShape(999.dp)
                                    )
                                    .clickable {
                                        overrideMood = if (moodLabel == "Auto") null else moodLabel
                                        displayedTracks =
                                            when (overrideMood ?: dominantMood) {
                                                "Chill" -> chillTracks.shuffled()
                                                "Hype" -> hypeTracks.shuffled()
                                                "Emotional" -> emotionalTracks.shuffled()
                                                else -> emptyList()
                                            }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = moodLabel,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = primaryText
                                )
                            }
                        }
                    }

                    // Mood Counter
                    Spacer(modifier = Modifier.padding(top = 12.dp))

                    val chillCount = moodCounts["Chill"] ?: 0
                    val hypeCount = moodCounts["Hype"] ?: 0
                    val emoCount = moodCounts["Emotional"] ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .padding(vertical = 9.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chill: $chillCount",
                            fontSize = 13.sp,
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Hype: $hypeCount",
                            fontSize = 13.sp,
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Emotional: $emoCount",
                            fontSize = 13.sp,
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.padding(top = 12.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(displayedTracks) { track ->
                    PlaylistTrackRow(
                        track = track,
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

@Composable
private fun PlaylistTrackRow(
    track: MoodTrack,
    cardColor: Color,
    primaryText: Color,
    secondaryText: Color,
    mutedText: Color,
    outline: Color
) {
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
            Text(
                text = track.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.padding(top = 2.dp))
            Text(
                text = track.artist,
                fontSize = 14.sp,
                color = secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.padding(top = 8.dp))
            Text(
                text = track.note,
                fontSize = 13.sp,
                color = mutedText,
                lineHeight = 18.sp
            )

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
