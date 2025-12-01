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
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope



// NOTE: SongChoice and Question are now imported from GameData.kt.

@Composable
fun GamePlayScreen(onGameComplete: () -> Unit) {
    var lives by remember { mutableStateOf(3) }
    var score by remember { mutableStateOf(0) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<SongChoice?>(null) }
    var isAnswerChecking by remember { mutableStateOf(false) }

    val currentQuestion = mockQuestions.getOrNull(currentQuestionIndex)

    if (lives <= 0 || currentQuestion == null) {
        GameOverScreen(score = score)
        onGameComplete()
        return
    }

    val options = currentQuestion.options
    val correctAnswer = options[currentQuestion.correctIndex]

    var shouldAdvanceQuestion by remember { mutableStateOf(false) }

    // Answer checking logic
    val handleAnswer: (SongChoice) -> Unit = handleAnswer@{ answer ->
        if (isAnswerChecking) return@handleAnswer

        isAnswerChecking = true
        selectedOption = answer

        if (answer == correctAnswer) {
            score += 10
        } else {
            lives -= 1
        }

        shouldAdvanceQuestion = true
    }


    LaunchedEffect(shouldAdvanceQuestion) {
        if (shouldAdvanceQuestion) {
            delay(1000)
            currentQuestionIndex++
            selectedOption = null
            isAnswerChecking = false
            shouldAdvanceQuestion = false
        }
    }

    // UI Layout...
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF59168B), Color(0xFF1C398E), Color(0xFF312C85))
                )
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lives: $lives", color = Color.White)
                Text("Score: $score", color = Color.Yellow)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "What song is playing? (Q${currentQuestionIndex + 1})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    options.forEach { song ->
                        ChoiceButton(
                            song = song,
                            isSelected = selectedOption == song,
                            isEnabled = !isAnswerChecking,
                            onClick = { handleAnswer(song) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ChoiceButton(
    song: SongChoice,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !isEnabled -> Color.Gray.copy(alpha = 0.5f)
        isSelected -> Color(0xFF59168B)
        else -> Color.Black
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = song.title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = song.artist, color = Color.LightGray)
        }
    }
}

@Composable
fun GameOverScreen(score: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Game Over!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = "Final Score: $score", fontSize = 18.sp)
            }
        }
    }
}