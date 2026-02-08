package np.ict.mad.mad25_p03_team03.ui

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import np.ict.mad.mad25_p03_team03.utils.PitchDetector
import np.ict.mad.mad25_p03_team03.utils.SoundGenerator
import kotlin.math.abs

// Variable - Color Theme - Custom colors for the dark theme UI
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White
private val SuccessGreen = Color(0xFF4CAF50)
private val ErrorRed = Color(0xFFCF6679)


// Variable - Game Config - The sequence of notes to mimic
val mimicLevels = listOf(
    MimicLevel("Do", "C4", 261.63),
    MimicLevel("Re", "D4", 293.66),
    MimicLevel("Mi", "E4", 329.63),
    MimicLevel("Fa", "F4", 349.23),
    MimicLevel("Sol", "G4", 392.00),
    MimicLevel("La", "A4", 440.00)
)

// Function - Main Screen - The Voice-Controlled Game Engine
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerMimicGameScreen(
    roomId: String,            // Variable - Input - The ID of the room in Firestore
    onNavigateBack: () -> Unit // Variable - Input - Callback to exit
) {
    // Flow 1.1: Dependency Setup
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val myId = currentUser?.uid ?: ""
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Variable - State - Game Logic
    var currentLevelIndex by remember { mutableStateOf(0) }
    val currentLevel = mimicLevels[currentLevelIndex % mimicLevels.size]

    // Variable - State - Audio Processing
    var isListening by remember { mutableStateOf(false) }
    var currentPitch by remember { mutableStateOf(0f) }
    var currentNoteName by remember { mutableStateOf("--") }
    var matchProgress by remember { mutableStateOf(0f) }
    val pitchDetector = remember { PitchDetector() }

    // Variable - State - Room Data
    var roomData by remember { mutableStateOf<Map<String, Any>?>(null) }
    val player1Id = roomData?.get("player1Id") as? String
    val ballPosition = (roomData?.get("ballPosition") as? Long)?.toInt() ?: 0
    val isPlayer1 = myId == player1Id
    val status = roomData?.get("status") as? String ?: "waiting"

    // Flow 2.0: Permission Handling
    // Asks user for microphone access to detect pitch
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isListening = true
            try {
                // Logic - Start Pitch Detection
                pitchDetector.start { hz, note ->
                    if (isListening) {
                        currentPitch = hz
                        currentNoteName = note
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // Flow 1.2: Cleanup
    DisposableEffect(Unit) {
        onDispose { pitchDetector.stop() }
    }

    // Function - Game Logic - Handle Exit
    // Flow 3.0: Exit Strategy
    val handleExit = {
        if (player1Id == myId) db.collection("pvp_rooms").document(roomId).delete()
        else db.collection("pvp_rooms").document(roomId).update("player2Id", null)
        onNavigateBack()
    }
    BackHandler { handleExit() }

    // Flow 4.0: Real-time Sync
    LaunchedEffect(roomId) {
        db.collection("pvp_rooms").document(roomId).addSnapshotListener { s, _ ->
            if (s != null && s.exists()) roomData = s.data else onNavigateBack()
        }
    }

    // Function - Audio Logic - Play Reference Tone
    // Flow 5.0: Sound Generation
    fun playTargetSound() {
        scope.launch {
            val wasListening = isListening
            isListening = false // Pause detection to avoid self-feedback

            // Logic - Play Tone
            SoundGenerator.playTone(currentLevel.frequency, 800)

            delay(200)
            if (wasListening) isListening = true
        }
    }

    // Flow 6.0: Start Listening
    LaunchedEffect(status) {
        if (status == "playing" && !isListening) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Function - Game Logic - Pitch Matching
    // Flow 7.0: Scoring Logic
    // Checks if the user's pitch matches the target frequency
    LaunchedEffect(currentPitch) {
        if (isListening && status == "playing" && currentPitch > 0) {
            val diff = abs(currentPitch - currentLevel.frequency)

            // Logic - Tolerance Check (within 20Hz)
            if (diff < 20.0) {
                matchProgress += 0.1f // Increment progress bar

                // Logic - Win Condition (Hold note for ~1 second)
                if (matchProgress >= 1f) {
                    matchProgress = 0f
                    currentLevelIndex++
                    playTargetSound() // Play next note

                    // Logic - Update Game State (Push the Ball)
                    db.runTransaction { transaction ->
                        val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
                        val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0
                        val direction = if (isPlayer1) 1 else -1
                        var newPos = currentPos + direction

                        // Clamp values
                        if (newPos > 2) newPos = 2
                        if (newPos < -2) newPos = -2

                        val updates = mutableMapOf<String, Any>("ballPosition" to newPos)

                        // Check overall winner
                        if (newPos >= 2) { updates["status"] = "finished"; updates["winnerId"] = player1Id ?: "" }
                        if (newPos <= -2) { updates["status"] = "finished"; updates["winnerId"] = "opponent" }

                        transaction.update(db.collection("pvp_rooms").document(roomId), updates)
                    }
                }
            } else {
                // Decay progress if pitch drifts
                if (matchProgress > 0) matchProgress -= 0.02f
            }
        }
    }

    // Flow 8.0: UI Construction
    Scaffold(
        containerColor = DarkBackground1, // Variable - Color - Dark Background
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Fast Mimic Battle", color = TextWhite, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground1
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(DarkBackground1), // Ensure background is dark
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Flow 8.1: Gameplay UI
            if (status == "playing") {

                Text("PUSH WITH YOUR VOICE!", fontWeight = FontWeight.Bold, color = PurpleAccent)
                Spacer(Modifier.height(8.dp))

                // UI - Tug of War Bar
                LinearProgressIndicator(
                    progress = { (ballPosition + 2) / 4f },
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = if(isPlayer1) PurpleAccent else ErrorRed,
                    trackColor = CardColor1
                )

                // UI - Position Labels
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("YOU", fontWeight = FontWeight.Bold, color = if(isPlayer1) PurpleAccent else ErrorRed)
                    Text("SLOT 1", fontSize = 10.sp, color = Color.Gray)
                    Text("MID", fontSize = 10.sp, color = Color.Gray)
                    Text("SLOT 1", fontSize = 10.sp, color = Color.Gray)
                    Text("ENEMY", fontWeight = FontWeight.Bold, color = if(isPlayer1) ErrorRed else PurpleAccent)
                }

                Spacer(Modifier.height(30.dp))

                // UI - Target Note Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardColor1) // Variable - Color - Secondary Dark
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Sing this note:", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                        Text(
                            currentLevel.name,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                        Text(
                            "(${currentLevel.targetNote})",
                            style = MaterialTheme.typography.titleMedium,
                            color = PurpleAccent
                        )

                        Spacer(Modifier.height(16.dp))

                        // UI - Play Tone Button
                        Button(
                            onClick = { playTargetSound() },
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = TextWhite)
                            Spacer(Modifier.width(8.dp))
                            Text("Play Tone", color = TextWhite)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // UI - Pitch Feedback Visualization
                Text(
                    "You: $currentNoteName (${currentPitch.toInt()} Hz)",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextWhite
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(CardColor1, CircleShape)
                ) {
                    // Center Marker
                    Box(Modifier.align(Alignment.Center).width(2.dp).fillMaxHeight().background(Color.Gray))

                    // Moving Cursor (Your Pitch)
                    val diff = (currentPitch - currentLevel.frequency).coerceIn(-100.0, 100.0)
                    val offsetX = (diff / 100.0) * 150

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = offsetX.dp)
                            .size(24.dp)
                            .background(if (abs(diff) < 20) SuccessGreen else ErrorRed, CircleShape)
                            .border(2.dp, TextWhite, CircleShape)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // UI - Hold Progress Bar
                Text("Hold steady...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                LinearProgressIndicator(
                    progress = { matchProgress },
                    modifier = Modifier.fillMaxWidth().height(20.dp),
                    color = SuccessGreen,
                    trackColor = Color.DarkGray
                )

                Spacer(Modifier.weight(1f))

            }
            // Flow 8.2: Game Over UI
            else if (status == "finished") {

                val iWon = (ballPosition >= 10 && isPlayer1) || (ballPosition <= -10 && !isPlayer1)

                Spacer(Modifier.height(40.dp))

                Text(
                    if (iWon) "VICTORY! 🎤" else "DEFEAT...",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = if(iWon) SuccessGreen else ErrorRed
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = handleExit,
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                ) {
                    Text("Back to Lobby", color = TextWhite)
                }
            }
            // Flow 8.3: Loading UI
            else {
                CircularProgressIndicator(color = PurpleAccent)
                Text("Waiting for opponent...", color = TextWhite, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}