package np.ict.mad.mad25_p03_team03

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.MusicNote

// Data class to represent a song choice
data class SongChoice(val title: String, val artist: String)

@Composable
fun GamePlayScreen() {
    // --- Mock State Variables ---
    val lives by remember { mutableStateOf(3) }
    val score by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(10) } // Time remaining for the current question
    var selectedOption by remember { mutableStateOf<SongChoice?>(null) }

    // Mock data for the options based on your image
    val songOptions = remember {
        listOf(
            SongChoice("Levitating", "Dua Lipa"),
            SongChoice("Shape of You", "Ed Sheeran"),
            SongChoice("Good 4 U", "Olivia Rodrigo"),
            SongChoice("Heat Waves", "Glass Animals")
        )
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF59168B), // Purple
            Color(0xFF1C398E), // Blue
            Color(0xFF312C85)  // Dark Purple
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- Header Section ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Music Icon",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Song Guessing Game",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
            Text(
                text = "Test your music knowledge!",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xCCFFFFFF),
                modifier = Modifier.padding(start = 40.dp, bottom = 32.dp)
            )

            // --- Menu Buttons (Play Game / Song Library) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Play Game Button (Active)
                Button(
                    onClick = { /* Currently on this screen */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp).padding(end = 8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play Icon", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play Game")
                }

                // Song Library Button
                Button(
                    onClick = { /* Navigate to Song Library */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(imageVector = Icons.Filled.List, contentDescription = "Library Icon", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Song Library")
                }
            }


            // --- Game Card ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp) // Fixed height to fit all content based on the design
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Score and Lives Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Song\nGuessing\nGame",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 28.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Lives Icons
                            for (i in 1..3) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Life $i",
                                    tint = if (i <= lives) Color.Red else Color.Gray,
                                    modifier = Modifier.padding(horizontal = 2.dp).size(24.dp)
                                )
                            }
                            // Score Text
                            Text(
                                text = "Score: $score",
                                color = Color.Yellow, // Use a themed color if possible
                                modifier = Modifier.padding(start = 16.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Time Remaining Bar
                    Text(
                        text = "Time Remaining",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Timer Bar (using a simple LinearProgressIndicator for visualization)
                        LinearProgressIndicator(
                            progress = { timeLeft / 10f }, // Assuming 10 is the max time
                            modifier = Modifier.weight(1f).height(10.dp),
                            color = Color(0xFF59168B),
                            trackColor = Color.LightGray
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = "$timeLeft s", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Pause Button
                    Button(
                        onClick = { isPaused = !isPaused /* Implement pause logic */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray.copy(alpha = 0.6f),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isPaused) "Resume" else "Pause", fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question Text
                    Text(
                        text = "What song is playing?",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // --- Multiple Choice Options ---
                    songOptions.forEach { song ->
                        ChoiceButton(
                            song = song,
                            isSelected = selectedOption == song,
                            onClick = { selectedOption = song } // State change on click
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChoiceButton(song: SongChoice, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF59168B) else Color.Black
    val borderColor = if (isSelected) Color(0xFF4C6FFF) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable(onClick = onClick)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = song.artist,
                color = Color.LightGray,
                fontSize = 12.sp
            )
        }
    }
}

@Preview
@Composable
fun PreviewGamePlayScreen() {
    GamePlayScreen()
}