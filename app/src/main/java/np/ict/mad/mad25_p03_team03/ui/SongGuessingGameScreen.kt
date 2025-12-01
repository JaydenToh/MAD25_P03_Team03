package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun SongGuessingGameScreen(
    onStartGameClick: () -> Unit,
    onSongLibraryClick: () -> Unit
) {
    // State variables
    val lives = remember { mutableStateOf(3) }
    val score = remember { mutableStateOf(0) }
    var isGameStarted by remember { mutableStateOf(false) }

    // UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(
                colors = listOf(Color(0xFF59168B), Color(0xFF1C398E), Color(0xFF312C85))
            ))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Title
            Text(
                text = "Song Guessing Game",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Game Info Card
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Lives and Score
                    Row(horizontalArrangement = Arrangement.SpaceBetween) {
                        // Lives display
                        Row {
                            repeat(3) { i ->
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    tint = if (i < lives.value) Color.Red else Color.Gray
                                )
                            }
                        }
                        Text(text = "Score: ${score.value}", color = Color.Yellow)
                    }

                    // Description
                    Text(text = "Listen to clips and guess the songs!")

                    // Start Button
                    Button(
                        onClick = {
                            isGameStarted = true
                            onStartGameClick()
                        }
                    ) {
                        Text("Start Game")
                    }

                    // Library Button
                    Button(onClick = onSongLibraryClick) {
                        Text("Song Library")
                    }
                }
            }
        }
    }
}