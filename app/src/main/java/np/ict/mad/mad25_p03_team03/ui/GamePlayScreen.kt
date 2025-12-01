/*package np.ict.mad.mad25_p03_team03.ui

import SongDto
import np.ict.mad.mad25_p03_team03.data.repository.SongRepository
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import SongChoice
import np.ict.mad.mad25_p03_team03.data.SongChoice.Question



// NOTE: SongChoice and np.ict.mad.mad25_p03_team03.data.Question are now imported from GameData.kt.

@Composable
fun GamePlayScreen(
    songRepository: np.ict.mad.mad25_p03_team03.data.repository.SongRepository,
    onGameComplete: () -> Unit
) {
    var songs by remember { mutableStateOf<List<SongDto>>(emptyList()) }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    // 加载数据
    LaunchedEffect(Unit) {
        try {
            val songList = songRepository.getSongs()
            songs = songList
            questions = generateQuestionsFromSongs(songList)
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            hasError = true
            isLoading = false
        }
    }

    // Current question (only if data is loaded)
    val currentQuestion = questions.getOrNull(currentQuestionIndex)

    // Game Over
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

    // Advance to next question after delay
    LaunchedEffect(shouldAdvanceQuestion) {
        if (shouldAdvanceQuestion) {
            delay(1000)
            currentQuestionIndex++
            selectedOption = null
            isAnswerChecking = false
            shouldAdvanceQuestion = false
        }
    }

    // Loading State
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Text("Loading songs...", color = Color.White, modifier = Modifier.padding(top = 16.dp))
            }
        }
        return
    }

    // Error State
    if (hasError) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Failed to load songs", color = Color.Red)
                Button(onClick = {
                    // Retry loading
                    hasError = false
                    isLoading = true
                }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    // Main Game UI
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
                        text = "What song is playing? (Q${currentQuestionIndex + 1}/${questions.size})",
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

// Helper function to generate questions from Firebase songs
private fun generateQuestionsFromSongs(songs: List<SongDto>): List<Question> {
    if (songs.size < 4) return emptyList()

    val questions = mutableListOf<Question>()

    songs.forEach { correctSong ->
        // 获取3个错误答案
        val wrongAnswers = songs
            .filterNot { it.id == correctSong.id }
            .shuffled()
            .take(3)
            .map {
                SongChoice(
                    title = it.title ?: "",
                    artist = it.artist ?: "",
                    url = it.url ?: ""  // ✅ 现在可以传递 url 了
                )
            }

        // 创建选项列表
        val allOptions = mutableListOf<SongChoice>().apply {
            add(
                SongChoice(
                    title = correctSong.title ?: "",
                    artist = correctSong.artist ?: "",
                    url = correctSong.url ?: ""  // ✅ 正确传递 url
                )
            )
            addAll(wrongAnswers)
        }.shuffled()

        // 找到正确答案的索引
        val correctIndex = allOptions.indexOfFirst {
            it.title == correctSong.title && it.artist == correctSong.artist
        }

        questions.add(
            Question(
                songUrl = correctSong.url ?: "",  // ✅ 正确传递 songUrl
                options = allOptions,
                correctIndex = correctIndex
            )
        )
    }

    return questions
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
}*/