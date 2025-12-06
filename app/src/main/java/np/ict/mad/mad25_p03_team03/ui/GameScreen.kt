// np/ict/mad/mad25_p03_team03/ui/GameScreen.kt

package np.ict.mad.mad25_p03_team03.ui

import android.media.MediaPlayer
import android.os.CountDownTimer
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.ict.mad.mad25_p03_team03.data.SongRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    songRepository: SongRepository,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var questions by remember { mutableStateOf<List<SongQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(40) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentTimer by remember { mutableStateOf<CountDownTimer?>(null) }

    val currentQuestion = questions.getOrNull(currentIndex)

    fun playAudio(url: String?) {
        val cleanUrl = url?.trim() ?: return
        if (cleanUrl.isEmpty()) return

        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }

        try {
            val mp = MediaPlayer().apply {
                setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
                setDataSource(cleanUrl)
                setOnPreparedListener { it.start() }
                setOnCompletionListener { release(); mediaPlayer = null }
                setOnErrorListener { _, _, _ -> release(); mediaPlayer = null; true }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            e.printStackTrace()
            mediaPlayer = null
        }
    }

    // load questions from Supabase
    LaunchedEffect(Unit) {
        isLoading = true
        val remoteSongs = songRepository.fetchSongsFromSupabase()
        questions = if (remoteSongs.isNotEmpty()) {
            remoteSongs.map { songDto ->
                val options = (listOf(songDto.title) + songDto.fakeOptions).shuffled().take(4)
                SongQuestion(songDto.title, options, songDto.audioUrl)
            }
        } else {
            listOf(
                SongQuestion(
                    "Blinding Lights",
                    listOf("Blinding Lights", "Save Your Tears", "Levitating", "Peaches"),
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3" // ✅ 无空格
                ),
                SongQuestion(
                    "Bohemian Rhapsody",
                    listOf("Bohemian Rhapsody", "Stairway to Heaven", "Hotel California", "Imagine"),
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3" // ✅ 无空格
                )
            )
        }
        isLoading = false
    }

    // Timer logic
    LaunchedEffect(currentIndex, isLoading) {
        if (!isLoading && currentIndex < questions.size) {
            currentTimer?.cancel()
            timeLeft = 40
            playAudio(questions[currentIndex].audioUrl)

            val timer = object : CountDownTimer(40_000, 1_000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (!isGameOver) { // extra check
                        timeLeft = (millisUntilFinished / 1000).toInt()
                    }
                }
                override fun onFinish() {
                    if (isGameOver) return
                    timeLeft = 0
                    if (lives > 0 && currentIndex < questions.size) {
                        lives -= 1
                        message = "⏰ Time's up!"
                        if (lives <= 0) {
                            isGameOver = true // mark game over
                        } else if (currentIndex < questions.lastIndex) {
                            currentIndex += 1
                        }
                    }
                }
            }
            timer.start()
            currentTimer = timer
        }
    }

    fun advanceToNextQuestion(isCorrect: Boolean) {
        if (isGameOver) return

        currentTimer?.cancel()
        if (isCorrect) {
            score += 10
            message = "✅ Correct!"
        } else {
            lives -= 1
            message = "❌ Wrong!"
            if (lives <= 0) {               // case lives run out
                isGameOver = true           // mark game over
            }
        }

        if (!isGameOver && currentIndex < questions.lastIndex) {
            currentIndex += 1
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentTimer?.cancel()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // ✅ UI with emoji prompt
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🎵 Song Guesser", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                    Text("Loading songs...")
                } else if (currentQuestion != null) {
                    // status row: score, lives, time
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Score: $score", style = MaterialTheme.typography.bodyLarge)
                        Text("Lives: $lives", style = MaterialTheme.typography.bodyLarge)
                        Text("Time: $timeLeft", style = MaterialTheme.typography.bodyLarge)
                    }

                    // improved title with emoji
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎵 What is this song? 🎶",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "🎧 Select your correct answer below ⬇️",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Replay button: height reduced to 48dp for better spacing
                    Button(
                        onClick = { playAudio(currentQuestion.audioUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp), // 👈 从 56dp → 48dp
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("▶️ Replay Song Clip", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    // options buttons
                    currentQuestion.options.forEach { option ->
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            onClick = {
                                advanceToNextQuestion(option == currentQuestion.correctTitle)
                            },
                            enabled = !isGameOver // Disable if game over
                        ) {
                            Text(option, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // prompt message
                    if (message.isNotEmpty()) {
                        Text(
                            message,
                            color = if (message.contains("Correct")) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // game over / success message
                    val isAllDone = currentIndex >= questions.size
                    val isSuccess = isAllDone && lives > 0

                    if (lives <= 0 || isAllDone) {
                        Spacer(Modifier.weight(1f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isSuccess) "🎉 Success!" else "😢 Game Over",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSuccess) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                            Text("Final Score: $score", style = MaterialTheme.typography.titleLarge)

                            if (lives <= 0) {
                                Spacer(Modifier.height(24.dp))
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            currentIndex = 0
                                            score = 0
                                            lives = 3
                                            message = ""
                                        },
                                        modifier = Modifier.height(56.dp)
                                    ) {
                                        Text("↺ Restart", fontSize = 16.sp)
                                    }

                                    // back to rules button
                                    Button(
                                        onClick = onNavigateBack,
                                        modifier = Modifier.height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text("← Back to Rules", fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    )
}

data class SongQuestion(
    val correctTitle: String,
    val options: List<String>,
    val audioUrl: String?
)