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

// Variable - Color - Custom background color for this screen
private val DarkBackground3 = Color(0xFF121212)
private val CardColor3 = Color(0xFF2F2F45 )

// Function - UI Component - The Main Game Screen logic
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    songRepository: SongRepository, // Variable - Input - Repository to fetch song data
    gameMode: GameMode,             // Variable - Input - Selected language mode (English/Mandarin)
    difficulty: Difficulty,         // Variable - Input - Selected difficulty (Time limit)
    onNavigateBack: () -> Unit      // Variable - Input - Callback function to handle back navigation
) {
    // Flow 1.1: Dependency Injection
    // Get context for media player and Firebase instances for score saving
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Variable - State - Holds the list of questions for the game
    var questions by remember { mutableStateOf<List<SongQuestion>>(emptyList()) }
    // Variable - State - Indicates if data is still loading
    var isLoading by remember { mutableStateOf(true) }

    // Variable - State - Game Progress Trackers
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var currentStreak by remember { mutableStateOf(0) }
    var longestStreak by remember { mutableStateOf(0) }
    var correctCount by remember { mutableStateOf(0) }
    var totalTimeTaken by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Variable - State - Timer and Audio logic
    var timeLeft by remember { mutableStateOf(difficulty.timeLimitSeconds) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentTimer by remember { mutableStateOf<CountDownTimer?>(null) }
    var hasSavedScore by remember { mutableStateOf(false) }

    // Flow 1.2: Derived State Calculation
    // Check if all questions are answered or if game over flag is set
    val isAllQuestionsAnswered = !isLoading && questions.isNotEmpty() && currentIndex >= questions.size
    val isGameFinished = isGameOver || isAllQuestionsAnswered
    val currentQuestion = if (!isGameFinished) questions.getOrNull(currentIndex) else null

    // Function - Internal Logic - Standardized audio playback function
    // Flow 2.0: Audio Manager
    fun playAudio(url: String?) {
        // Flow 2.1: URL Validation
        val cleanUrl = url?.trim() ?: return
        if (cleanUrl.isEmpty()) return

        // Flow 2.2: Reset Previous Player
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }

        try {
            // Flow 2.3: Initialize New Player
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

    // Function - Internal Logic - Play Game Over Sound Effect
    fun playGameOverSound() {
        // Flow 2.4: Stop Music
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        try {
            // Flow 2.5: Play SFX
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

    // Function - Internal Logic - Play Success Sound Effect
    fun playSuccessSound() {
        // Flow 2.6: Stop Music
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        try {
            // Flow 2.7: Play SFX
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

    // Flow 3.0: Data Loading (Side Effect)
    LaunchedEffect(Unit) {
        isLoading = true
        // Flow 3.1: Fetch from Supabase
        val remoteSongs = songRepository.fetchSongsFromSupabase(gameMode)

        // Flow 3.2: Map Data to Questions
        questions = if (remoteSongs.isNotEmpty()) {
            remoteSongs.map { songDto ->
                // Shuffle options to ensure randomness
                val options = (listOf(songDto.title) + songDto.fakeOptions).shuffled().take(4)
                SongQuestion(songDto.title, options, songDto.audioUrl)
            }
        } else {
            // Flow 3.3: Fallback Mock Data (if fetch fails)
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

    // Flow 4.0: Game Loop & Timer
    // Triggered whenever currentIndex changes (next question)
    LaunchedEffect(currentIndex, isLoading, isGameFinished) {
        if (!isLoading && !isGameFinished && currentIndex < questions.size) {
            // Flow 4.1: Reset Timer
            currentTimer?.cancel()

            val maxTime = difficulty.timeLimitSeconds
            timeLeft = maxTime

            // Flow 4.2: Start Audio for Question
            playAudio(questions[currentIndex].audioUrl)

            // Flow 4.3: Start Countdown
            val timer = object : CountDownTimer(maxTime * 1000L, 1_000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (!isGameOver) {
                        timeLeft = (millisUntilFinished / 1000).toInt()
                    }
                }

                override fun onFinish() {
                    // Flow 4.4: Timeout Logic
                    if (isGameOver) return
                    timeLeft = 0
                    if (lives > 0) {
                        lives -= 1
                        message = "⏰ Time's up!"
                        // Check for Game Over
                        if (lives <= 0) {
                            isGameOver = true
                            playGameOverSound()
                        } else {
                            // Move to next question if lives remain
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

    // Function - Game Logic - Processes user's answer
    // Flow 5.0: Answer Handling
    fun advanceToNextQuestion(isCorrect: Boolean) {
        if (isGameFinished) return

        // Flow 5.1: Stop Timer
        currentTimer?.cancel()

        val timeUsed = difficulty.timeLimitSeconds - timeLeft
        totalTimeTaken += timeUsed

        // Flow 5.2: Check Correctness
        if (isCorrect) {
            score += 10
            currentStreak += 1
            correctCount += 1
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak
            }
            message = "✅ Correct!"
        } else {
            // Flow 5.3: Wrong Answer Logic
            lives -= 1
            currentStreak = 0
            message = "❌ Wrong!"

            if (lives <= 0) {
                isGameOver = true
                playGameOverSound()
                return
            }
        }

        // Flow 5.4: Next Question
        currentIndex += 1

        if (lives > 0 && currentIndex >= questions.size) {
            playSuccessSound()
        }
    }

    // Flow 6.0: Score Persistence
    // Triggered when game finishes to save high score
    LaunchedEffect(isGameFinished) {
        if (isGameFinished && !hasSavedScore) {
            hasSavedScore = true
            val user = auth.currentUser
            if (user != null) {
                val userRef = db.collection("users").document(user.uid)

                // Flow 6.1: Database Transaction
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(userRef)
                    val currentHigh = snapshot.getLong("highScore") ?: 0

                    // Flow 6.2: Update if new score is higher
                    if (score > currentHigh) {
                        transaction.update(userRef, "highScore", score)
                    }
                }.addOnSuccessListener {
                    // Success callback
                }.addOnFailureListener {
                    // Failure callback
                }
            }
        }
    }

    // Flow 1.3: Lifecycle Cleanup
    DisposableEffect(Unit) {
        onDispose {
            currentTimer?.cancel()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Flow 7.0: UI Layout
    Scaffold(
        containerColor = DarkBackground3, // Variable - Color - Dark Background
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
                // Flow 7.1: Loading View
                if (isLoading) {
                    CircularProgressIndicator()
                    Text("Loading songs...")
                }
                // Flow 7.2: Active Game View
                else if (!isGameFinished && currentQuestion != null) {
                    // UI - Stats Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Score: $score", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text("Lives: $lives", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text("Time: $timeLeft", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    }

                    // UI - Question Header
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

                    // UI - Replay Button
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

                    // UI - Option Buttons
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

                    // UI - Feedback Message
                    if (message.isNotEmpty()) {
                        Text(
                            message,
                            color = if (message.contains("Correct")) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                // Flow 7.3: Game Summary View
                else if (isGameFinished) {
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
                            // Flow 8.0: Reset Logic
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