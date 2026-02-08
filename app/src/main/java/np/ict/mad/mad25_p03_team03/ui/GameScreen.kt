// np/ict/mad/mad25_p03_team03/ui/GameScreen.kt

package np.ict.mad.mad25_p03_team03.ui

import GameSummaryScreen
import android.media.MediaPlayer
import android.os.CountDownTimer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.R
import np.ict.mad.mad25_p03_team03.data.Difficulty
import np.ict.mad.mad25_p03_team03.data.GameMode

private val DarkBackground3 = Color(0xFF121212)
private val CardColor3 = Color(0xFF2F2F45 )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    songRepository: SongRepository,
    gameMode: GameMode,
    difficulty: Difficulty,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var questions by remember { mutableStateOf<List<SongQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var currentStreak by remember { mutableStateOf(0) }
    var longestStreak by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var totalTimeTaken by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(difficulty.timeLimitSeconds) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentTimer by remember { mutableStateOf<CountDownTimer?>(null) }
    var hasSavedScore by remember { mutableStateOf(false) }

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
        val remoteSongs = songRepository.fetchSongsFromSupabase(gameMode)
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

            val maxTime = difficulty.timeLimitSeconds
            timeLeft = maxTime

            playAudio(questions[currentIndex].audioUrl)

            val timer = object : CountDownTimer(maxTime * 1000L, 1_000) {
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

        val timeUsed = difficulty.timeLimitSeconds - timeLeft
        totalTimeTaken += timeUsed

        if (isCorrect) {
            score += 10
            currentStreak += 1
            correctCount += 1
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak
            }
            message = "✅ Correct!"
        } else {
            lives -= 1

            currentStreak = 0
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

    // save score to Firestore when game is finished
    LaunchedEffect(isGameFinished) {
        if (isGameFinished && !hasSavedScore) {
            hasSavedScore = true
            val user = auth.currentUser
            if (user != null) {
                val userRef = db.collection("users").document(user.uid)


                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentHigh = snapshot.getLong("highScore") ?: 0


                    if (score > currentHigh) {
                        transaction.update(userRef, "highScore", score)

                    }
                }.addOnSuccessListener {

                }.addOnFailureListener {

                }
            }
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
        containerColor = DarkBackground3,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("🎵 Song Guesser", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DarkBackground3)
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
                        Text("Score: $score", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text("Lives: $lives", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text("Time: $timeLeft", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎵 What is this song? 🎶",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "🎧 Select your correct answer below",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Button(
                        onClick = { playAudio(currentQuestion.audioUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF651FFF)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Replay Song Clip",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    currentQuestion.options.forEach { option ->
                        Button(
                            onClick = { advanceToNextQuestion(option == currentQuestion.correctTitle) },
                            colors = ButtonDefaults.buttonColors(containerColor = CardColor3),
                            shape = RoundedCornerShape(25.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Text(option, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Medium)
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
                    val attempts = currentIndex
                    val avgTime = if (attempts > 0) totalTimeTaken.toFloat() / attempts else 0f


                    GameSummaryScreen(
                        score = score,
                        totalQuestions = attempts,
                        correctCount = correctCount,
                        longestStreak = longestStreak,
                        avgTime = avgTime,
                        isWin = lives > 0,
                        onPlayAgain = {
                            currentIndex = 0
                            score = 0
                            lives = 3
                            currentStreak = 0
                            longestStreak = 0
                            correctCount = 0
                            totalTimeTaken = 0
                            message = ""
                            isGameOver = false
                            hasSavedScore = false
                            timeLeft = difficulty.timeLimitSeconds
                        },
                        onBack = onNavigateBack
                    )
                }
            }
        }
    )
}

