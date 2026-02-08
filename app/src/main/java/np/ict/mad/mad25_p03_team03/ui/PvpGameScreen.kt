package np.ict.mad.mad25_p03_team03.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import np.ict.mad.mad25_p03_team03.data.GameMode
import np.ict.mad.mad25_p03_team03.data.SongRepository
import androidx.compose.runtime.DisposableEffect


// Variable - Color Theme - Custom colors for the dark theme UI
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White
private val SuccessGreen = Color(0xFF4CAF50)
private val ErrorRed = Color(0xFFCF6679)

// Class - Data Model - Structure for a single question
data class SongQuestion(
    val correctTitle: String,
    val options: List<String>,
    val audioUrl: String?
)

// Function - Main Screen - The "Tug of War" style PvP Game Screen
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PvpGameScreen(
    roomId: String,                 // Variable - Input - The ID of the room in Firestore
    songRepository: SongRepository, // Variable - Input - To fetch songs if hosting
    onNavigateBack: () -> Unit      // Variable - Input - Callback to exit
) {
    // Flow 1.1: Dependency Setup
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val myId = currentUser?.uid ?: ""
    val context = LocalContext.current

    // Variable - State - Holds all raw data from the Firestore room document
    var roomData by remember { mutableStateOf<Map<String, Any>?>(null) }
    // Variable - State - The list of questions for this match
    var questions by remember { mutableStateOf<List<SongQuestion>>(emptyList()) }
    // Variable - State - Status message displayed to the user
    var message by remember { mutableStateOf("Waiting for opponent...") }
    // Variable - State - Media Player for playing song snippets
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // Variable - Derived State - Extract specific fields from roomData
    val player1Id = roomData?.get("player1Id") as? String
    val player2Id = roomData?.get("player2Id") as? String
    val status = roomData?.get("status") as? String ?: "waiting"
    val currentIdx = (roomData?.get("currentQuestionIndex") as? Long)?.toInt() ?: 0

    // Variable - Game Logic - Check if we are Player 1 (Host)
    val isPlayer1 = myId == player1Id
    // Variable - Game Logic - Check if opponent is a Bot
    val isBotGame = player2Id == "BOT" || player2Id == "AI"

    // Variable - Game Logic - "Tug of War" Ball Position
    // 0 = Center
    // Positive (+) = Towards Player 2 (P1 is winning)
    // Negative (-) = Towards Player 1 (P2 is winning)
    val ballPosition = (roomData?.get("ballPosition") as? Long)?.toInt() ?: 0

    // Flow 1.2: Cleanup on Exit
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Function - Audio Logic - Plays the song from a URL
    // Flow 2.0: Audio System
    fun playAudio(url: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { start() }
                setOnErrorListener { _, _, _ -> true }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Function - Audio Logic - Stops playback
    fun stopAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Function - Game Logic - Handles user leaving the screen
    // Flow 3.0: Exit Strategy
    val handleExit = {
        if (player1Id == myId) {
            // Logic - Host deletes the room
            db.collection("pvp_rooms").document(roomId).delete()
        } else {
            // Logic - Guest just leaves (resets room to waiting)
            if (status == "waiting" || status == "playing") {
                db.collection("pvp_rooms").document(roomId).update(
                    mapOf("player2Id" to null, "status" to "waiting", "ballPosition" to 0)
                )
            }
        }
        onNavigateBack()
    }

    BackHandler { handleExit() }

    // Flow 4.0: Real-time Room Sync
    // Listens for changes in the Firestore document
    LaunchedEffect(roomId) {
        val docRef = db.collection("pvp_rooms").document(roomId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                roomData = snapshot.data

                // Logic - Parse Questions
                val questionsData = snapshot.get("questions") as? List<Map<String, Any>>
                if (questionsData != null && questionsData.isNotEmpty()) {
                    questions = questionsData.map { q ->
                        SongQuestion(
                            correctTitle = q["correctTitle"] as String,
                            options = (q["options"] as List<*>).map { it.toString() },
                            audioUrl = q["audioUrl"] as String?
                        )
                    }
                }
            } else {
                Toast.makeText(context, "Room closed by host", Toast.LENGTH_SHORT).show()
                onNavigateBack()
            }
        }
    }

    // Flow 5.0: Audio Trigger Logic
    // Watches 'currentIdx' -> When it changes, play the new song
    LaunchedEffect(currentIdx, status, questions) {
        if (status == "playing" && questions.isNotEmpty()) {
            val currentQuestion = questions.getOrNull(currentIdx)
            val url = currentQuestion?.audioUrl

            if (!url.isNullOrEmpty()) {
                delay(300) // Small buffer
                playAudio(url)
            }
        } else {
            stopAudio()
        }
    }

    // Flow 6.0: Host Initialization Logic
    // Only Player 1 runs this to generate questions if they are missing
    LaunchedEffect(roomData) {
        val p1Id = roomData?.get("player1Id") as? String
        val questionsInRoom = roomData?.get("questions") as? List<*>

        if (p1Id == myId && (questionsInRoom == null || questionsInRoom.isEmpty())) {
            // Logic - Fetch Songs
            val songs = songRepository.fetchSongsFromSupabase(GameMode.ENGLISH).take(10)

            // Logic - Map to Questions
            val mappedQuestions = songs.map { song ->
                val options = (listOf(song.title) + song.fakeOptions).shuffled().take(4)
                mapOf("correctTitle" to song.title, "options" to options, "audioUrl" to song.audioUrl)
            }

            // Logic - Upload to Firestore
            db.collection("pvp_rooms").document(roomId).update(
                mapOf(
                    "questions" to mappedQuestions,
                    "ballPosition" to 0
                )
            )
        }
    }

    val roundWinnerId = roomData?.get("roundWinnerId") as? String
    val gameWinnerId = roomData?.get("winnerId") as? String

    // Function - Game Logic - Processes a user's answer
    // Flow 7.0: Answer Submission & Ball Movement
    fun submitAnswer(selectedOption: String) {
        // Logic - Validation
        if (status != "playing" || roundWinnerId != null || gameWinnerId != null) return
        val currentQuestion = questions.getOrNull(currentIdx) ?: return

        if (selectedOption == currentQuestion.correctTitle) {
            // Logic - Atomic Transaction for Fairness
            db.runTransaction { transaction ->
                val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))

                // Only proceed if no one has won this round yet
                if (snapshot.getString("roundWinnerId") == null) {
                    val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

                    // Logic - Move Ball
                    // P1 moves +1, P2 moves -1
                    var newPos = if (isPlayer1) currentPos + 1 else currentPos - 1

                    // Logic - Clamp Values (-3 to 3)
                    if (newPos > 3) newPos = 3
                    if (newPos < -3) newPos = -3

                    val updates = mutableMapOf<String, Any>(
                        "roundWinnerId" to myId,
                        "ballPosition" to newPos
                    )

                    // Logic - Check Win Condition (Reaching 3 or -3)
                    if (newPos == 3) {
                        updates["winnerId"] = player1Id ?: "" // P1 Wins
                        updates["status"] = "finished"
                    } else if (newPos == -3) {
                        updates["winnerId"] = if (isPlayer1) "opponent" else myId // P2 Wins
                        updates["status"] = "finished"
                    }

                    transaction.update(db.collection("pvp_rooms").document(roomId), updates)
                }
            }
        } else {
            message = "Wrong answer! 😱"
        }
    }

    // Flow 8.0: Round Reset Logic
    LaunchedEffect(roundWinnerId) {
        if (roundWinnerId != null) {
            message = if (roundWinnerId == myId) "💪 PUSHED!" else "🛡️ PUSHED BACK!"
            delay(1500)

            // Logic - Host advances the round
            if (player1Id == myId && gameWinnerId == null) {
                if (currentIdx + 1 < questions.size) {
                    db.collection("pvp_rooms").document(roomId).update(
                        mapOf("currentQuestionIndex" to currentIdx + 1, "roundWinnerId" to null)
                    )
                } else {
                    db.collection("pvp_rooms").document(roomId).update("status", "finished")
                }
            }
        } else {
            message = "Push the ball to enemy! 💣"
        }
    }

    // Flow 9.0: Bot Logic Injection
    TriviaBotLogic(
        roomId = roomId,
        status = status,
        isPlayer1 = isPlayer1,
        isBotGame = isBotGame,
        currentQuestionIndex = currentIdx
    )

    // Flow 10.0: UI Construction
    Scaffold(
        containerColor = DarkBackground1, // Variable - Color - Dark Background
        topBar = {
            if (status == "waiting" || status == "playing") {
                CenterAlignedTopAppBar(
                    title = { Text("Tug of War PVP", color = TextWhite) },
                    navigationIcon = {
                        IconButton(onClick = { handleExit() }) {
                            Text("❌", fontSize = 18.sp, color = TextWhite)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkBackground1
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(DarkBackground1),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flow 10.1: Waiting Screen
            if (status == "waiting") {
                CircularProgressIndicator(color = PurpleAccent)
                Text(
                    "Waiting for opponent...",
                    color = TextWhite,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            // Flow 10.2: Gameplay Screen
            else if (status == "playing") {

                Spacer(Modifier.height(16.dp))

                // UI - Ball Track Visualization
                BallTrackUI(ballPosition = ballPosition, isPlayer1 = isPlayer1)

                Spacer(Modifier.height(24.dp))

                val question = questions.getOrNull(currentIdx)
                if (question != null) {
                    Text(
                        "Question ${currentIdx + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))

                    // UI - Replay Button
                    Button(
                        onClick = {
                            val url = question.audioUrl
                            if (!url.isNullOrEmpty()) playAudio(url)
                            else Toast.makeText(context, "No audio available", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CardColor1)
                    ) {
                        Text("▶️ Replay Song", color = PurpleAccent)
                    }

                    Spacer(Modifier.height(16.dp))

                    // UI - Feedback Message
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PurpleAccent
                    )

                    Spacer(Modifier.height(16.dp))

                    // UI - Answer Buttons
                    // Fix: Hide options when round is finished (roundWinnerId != null) to prevent old words from persisting
                    if (roundWinnerId == null) {
                        question.options.forEach { option ->
                            Button(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                onClick = { submitAnswer(option) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PurpleAccent
                                )
                            ) {
                                Text(option, fontSize = 18.sp, color = TextWhite)
                            }
                        }
                    } else {
                        // Placeholder to maintain spacing during transition
                        Spacer(modifier = Modifier.height(50.dp))
                    }
                }
            }
            // Flow 10.3: Game Over Screen
            else {
                Spacer(Modifier.height(40.dp))
                Text(
                    "GAME OVER",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = TextWhite
                )
                Spacer(Modifier.height(24.dp))

                // Logic - Determine Winner based on ball position
                // ballPosition >= 3 -> P1 Won
                // ballPosition <= -3 -> P2 Won
                val didIWin = if (isPlayer1) (ballPosition >= 3) else (ballPosition <= -3)

                if (didIWin) {
                    Text("🏆 YOU WON!", style = MaterialTheme.typography.displayMedium, color = SuccessGreen)
                    Text("You smashed them!", style = MaterialTheme.typography.bodyLarge, color = TextWhite)
                } else {
                    Text("💀 YOU LOST", style = MaterialTheme.typography.displayMedium, color = ErrorRed)
                    Text("Crushed by the ball...", style = MaterialTheme.typography.bodyLarge, color = TextWhite)
                }

                Spacer(Modifier.height(48.dp))
                Button(
                    onClick = { handleExit() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                ) {
                    Text("Back to Lobby", color = TextWhite)
                }
            }
        }
    }
}

// Function - UI Component - Visualizes the "Tug of War" status
// Flow 11.0: Ball Track Component
@Composable
fun BallTrackUI(ballPosition: Int, isPlayer1: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Flow 11.1: Player Labels
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isPlayer1) "YOU" else "ENEMY",
                fontWeight = FontWeight.Bold,
                color = if (isPlayer1) PurpleAccent else ErrorRed
            )
            Text(
                text = if (isPlayer1) "ENEMY" else "YOU",
                fontWeight = FontWeight.Bold,
                color = if (isPlayer1) ErrorRed else PurpleAccent
            )
        }

        Spacer(Modifier.height(8.dp))

        // Flow 11.2: The Track and Ball
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("👤", fontSize = 24.sp)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(horizontal = 8.dp)
            ) {
                // UI - Center Line
                Divider(
                    modifier = Modifier.align(Alignment.Center),
                    thickness = 4.dp,
                    color = Color.Gray
                )

                // UI - Position Markers (-2, -1, 0, 1, 2)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.DarkGray, CircleShape)
                        )
                    }
                }

                // UI - The "Ball" (Bomb)
                // We use BiasAlignment to position the ball proportionally
                if (ballPosition in -2..2) {
                    val hBias = ballPosition / 2f

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(BiasAlignment(horizontalBias = hBias, verticalBias = 0f))
                                .size(34.dp)
                                .shadow(4.dp, CircleShape)
                                .background(Color.Black, CircleShape)
                                .border(2.dp, PurpleAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💣", fontSize = 18.sp)
                        }
                    }
                }
            }

            Text("👤", fontSize = 24.sp)
        }

        // Flow 11.3: Crush Status
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (ballPosition <= -3) "💥 CRUSHED!" else "",
                color = ErrorRed,
                fontWeight = FontWeight.Bold
            )

            Text(
                if (ballPosition >= 3) "💥 CRUSHED!" else "",
                color = ErrorRed,
                fontWeight = FontWeight.Bold
            )
        }
    }
}