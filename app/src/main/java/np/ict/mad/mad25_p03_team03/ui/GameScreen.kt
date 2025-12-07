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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.R

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

    val isAllQuestionsAnswered = !isLoading && questions.isNotEmpty() && currentIndex >= questions.size
    val isGameFinished = isGameOver || isAllQuestionsAnswered
    val currentQuestion = if (!isGameFinished) questions.getOrNull(currentIndex) else null

    // standadized audio playback function
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

    // game over sound effect
    fun playGameOverSound() {
        // make sure release the current MediaPlayer
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        try {
            // make sure you have res/raw/gameover.mp3 file
            val mp = MediaPlayer.create(context, R.raw.gameover)
            if (mp != null) {
                mp.setOnCompletionListener { player -> player.release() }
                mp.start()
            } else {
                // fallback: no sound
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playSuccessSound() {
        // stop and release any existing MediaPlayer
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        try {
            // make sure you have res/raw/completegame.mp3 file
            val resId = np.ict.mad.mad25_p03_team03.R.raw.completegame

            val mp = MediaPlayer.create(context, resId)
            if (mp != null) {
                mp.setOnCompletionListener { player -> player.release() }
                mp.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // load questions from repository
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
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                ),
                SongQuestion(
                    "Bohemian Rhapsody",
                    listOf("Bohemian Rhapsody", "Stairway to Heaven", "Hotel California", "Imagine"),
                    "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
                )
            )
        }
        isLoading = false
    }

    // countdown timer for each question
    LaunchedEffect(currentIndex, isLoading, isGameFinished) {
        if (!isLoading && !isGameFinished && currentIndex < questions.size) {
            currentTimer?.cancel()
            timeLeft = 40
            playAudio(questions[currentIndex].audioUrl)

            val timer = object : CountDownTimer(40_000, 1_000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (!isGameOver) {
                        timeLeft = (millisUntilFinished / 1000).toInt()
                    }
                }

                override fun onFinish() {
                    if (isGameOver) return
                    timeLeft = 0
                    if (lives > 0) {
                        lives -= 1
                        message = "⏰ Time's up!"
                        if (lives <= 0) {
                            isGameOver = true
                            playGameOverSound() // game over sound
                        } else {
                            currentIndex += 1

                            if (currentIndex >= questions.size) {
                                playSuccessSound()
                            }
                        }
                    }
                }
            }
            timer.start()
            currentTimer = timer
        }
    }

    // logic to advance to next question
    fun advanceToNextQuestion(isCorrect: Boolean) {
        if (isGameFinished) return

        currentTimer?.cancel()
        if (isCorrect) {
            score += 10
            message = "✅ Correct!"
        } else {
            lives -= 1
            message = "❌ Wrong!"
            if (lives <= 0) {
                isGameOver = true
                playGameOverSound() // play game over sound
                return
            }
        }
        currentIndex += 1

        if (lives > 0 && currentIndex >= questions.size) {
            playSuccessSound()
        }
    }

    // cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            currentTimer?.cancel()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // ui layout
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
                } else if (!isGameFinished && currentQuestion != null) {
                    // game in progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Score: $score", style = MaterialTheme.typography.bodyLarge)
                        Text("Lives: $lives", style = MaterialTheme.typography.bodyLarge)
                        Text("Time: $timeLeft", style = MaterialTheme.typography.bodyLarge)
                    }

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

                    Button(
                        onClick = { playAudio(currentQuestion.audioUrl) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("▶️ Replay Song Clip", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    }

                    currentQuestion.options.forEach { option ->
                        Button(
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            onClick = { advanceToNextQuestion(option == currentQuestion.correctTitle) }
                        ) {
                            Text(option, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    if (message.isNotEmpty()) {
                        Text(
                            message,
                            color = if (message.contains("Correct")) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (isGameFinished) {
                    // game over screen
                    Spacer(Modifier.weight(1f))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val isSuccess = lives > 0

                        Text(
                            text = if (isSuccess) "🎉 Success! 🎉" else "😢 Game Over",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        if (isSuccess) {
                            Text(
                                text = "Thank you for playing!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Text("Final Score: $score", style = MaterialTheme.typography.titleLarge)

                        Spacer(Modifier.height(24.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    currentIndex = 0
                                    score = 0
                                    lives = 3
                                    message = ""
                                    isGameOver = false
                                    timeLeft = 40
                                },
                                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp)
                            ) {
                                Text("↺ Play Again", fontSize = 18.sp)
                            }

                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("← Back to Rules", fontSize = 16.sp)
                            }
                        }
                    }
                    Spacer(Modifier.weight(1f))
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