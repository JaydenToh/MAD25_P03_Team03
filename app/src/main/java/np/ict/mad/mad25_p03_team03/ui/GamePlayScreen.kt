package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.ict.mad.mad25_p03_team03.data.SongChoice
import np.ict.mad.mad25_p03_team03.data.mockQuestions

// NOTE: SongChoice and Question are now imported from GameData.kt.

@Composable
fun GamePlayScreen() {
    // --- Game State Variables ---
    var lives by remember { mutableStateOf(3) }
    var score by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(10) }

    // 🚨 STATE CHANGES: Using mutable state for index and current question
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<SongChoice?>(null) }
    var isAnswerChecking by remember { mutableStateOf(false) } // Lock button clicks

    // Current Question data, pulled from GameData.kt
    val currentQuestion = mockQuestions.getOrNull(currentQuestionIndex)

    // Stop rendering and show Game Over if lives run out or no more questions
    if (lives <= 0 || currentQuestion == null) {
        // You need to implement the GameOverScreen composable
        // Placeholder:
        Text("GAME OVER! Score: $score", modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center), color = Color.White)
        return
    }

    val songOptions = currentQuestion.options // 🚨 NOW READS FROM DYNAMIC QUESTION

    // Coroutine to handle answer checking and question advancement
    val checkAnswer: (SongChoice) -> Unit = checkAnswer@{ answer ->
        // 🚨 FIX: Use the qualified return 'return@checkAnswer' to exit the lambda only.
        if (isAnswerChecking) return@checkAnswer
        isAnswerChecking = true
        selectedOption = answer // Highlight selected option

        val correctSong = currentQuestion.options[currentQuestion.correctIndex]

        if (answer == correctSong) {
            score += 10
        } else {
            lives -= 1
        }

        // Use a LaunchedEffect to move to the next question after a visual delay
        // This simulates the user waiting for the answer confirmation.
        // NOTE: In a real app, you'd use the CoroutineScope of the screen or an event bus.
        currentQuestionIndex = (currentQuestionIndex + 1) % mockQuestions.size

        // This part needs to be handled outside of the regular click handler
        // to manage the delay correctly. For now, we will advance immediately.
        selectedOption = null // Reset selection immediately for the next question
        isAnswerChecking = false
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF59168B), Color(0xFF1C398E), Color(0xFF312C85)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ... (Header and Menu Buttons remain the same) ...

            // --- Game Card ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Score and Lives Row (Same as before)

                    // ...

                    // Question Text
                    Text(
                        text = "What song is playing? (Q${currentQuestionIndex + 1})", // Show current question #
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // --- Multiple Choice Options ---
                    songOptions.forEach { song ->
                        ChoiceButton(
                            song = song,
                            isSelected = selectedOption == song,
                            // Disable clicks if already checking an answer
                            isEnabled = !isAnswerChecking,
                            onClick = {
                                if (!isAnswerChecking) {
                                    checkAnswer(song)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

// --- ChoiceButton Helper (Needs update to include isEnabled) ---
@Composable
fun ChoiceButton(song: SongChoice, isSelected: Boolean, isEnabled: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        !isEnabled -> Color.DarkGray.copy(alpha = 0.5f) // Dim when checking answer
        isSelected -> Color(0xFF59168B)
        else -> Color.Black
    }
    val borderColor = if (isSelected) Color(0xFF4C6FFF) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable(enabled = isEnabled, onClick = onClick)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = song.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = song.artist, color = Color.LightGray, fontSize = 12.sp)
        }
    }
}

@Preview
@Composable
fun PreviewGamePlayScreen() {
    GamePlayScreen()
}