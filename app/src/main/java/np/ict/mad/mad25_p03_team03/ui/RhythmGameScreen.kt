package np.ict.mad.mad25_p03_team03.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
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


// Variable - Color Theme - Custom colors for the dark theme UI
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White
private val SuccessGreen = Color(0xFF4CAF50)
private val ErrorRed = Color(0xFFCF6679)
private val ComboYellow = Color(0xFFFFD700)

// Function - Main Screen - The Rhythm Game Engine
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RhythmGameScreen(
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

    // Variable - State - Game Data
    var roomData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var feedbackText by remember { mutableStateOf("") } // Perfect / Miss
    var combo by remember { mutableStateOf(0) }

    // Variable - Derived State
    val player1Id = roomData?.get("player1Id") as? String
    val ballPosition = (roomData?.get("ballPosition") as? Long)?.toInt() ?: 0
    val isPlayer1 = myId == player1Id
    val songUrl = roomData?.get("currentSongUrl") as? String
    val status = roomData?.get("status") as? String ?: "waiting"
    val isBotGame = roomData?.get("player2Id") == "BOT"

    // Flow 2.0: Animation Loop
    // Moves the note across the screen endlessly
    val infiniteTransition = rememberInfiniteTransition(label = "rhythm")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), // Linear movement over 2 seconds
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    // Flow 1.2: Cleanup
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
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

    // Flow 5.0: Host Initialization
    // Host picks a random song to play
    LaunchedEffect(roomData) {
        if (isPlayer1 && songUrl == null) {
            val songs = songRepository.fetchSongsFromSupabase(GameMode.ENGLISH).take(1)
            if (songs.isNotEmpty()) {
                db.collection("pvp_rooms").document(roomId).update(
                    mapOf("currentSongUrl" to songs[0].audioUrl, "ballPosition" to 0)
                )
            }
        }
    }

    // Flow 6.0: Audio Management
    LaunchedEffect(songUrl, status) {
        if (status == "playing" && songUrl != null) {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                setDataSource(songUrl)
                isLooping = true
                prepareAsync()
                setOnPreparedListener { start() }
            }
        } else if (status == "finished") {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // Function - Game Logic - Hit Detection
    // Flow 7.0: Timing Logic
    // Defines the valid "hit windows" based on the animation progress (0.0 to 1.0)
    fun checkHit(): Boolean {
        val p = progress
        // Slot 1: 0.05 - 0.20
        // Slot 2: 0.30 - 0.45
        // Slot 3: 0.55 - 0.70
        // Slot 4: 0.80 - 0.95
        return (p in 0.05..0.20) || (p in 0.30..0.45) || (p in 0.55..0.70) || (p in 0.80..0.95)
    }

    // Function - Game Logic - Process Tap
    // Flow 8.0: Tap Processing
    fun onTap() {
        if (status != "playing") return

        var moveAmount = 0

        if (checkHit()) {
            feedbackText = "PERFECT! ⭐"
            combo++
            moveAmount = 1
        } else {
            feedbackText = "MISS... ❌"
            combo = 0
            moveAmount = -1
        }

        // Logic - Atomic Transaction
        db.runTransaction { transaction ->
            val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
            val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

            val playerDirection = if (isPlayer1) 1 else -1
            val actualMove = playerDirection * moveAmount
            var newPos = currentPos + actualMove

            // Clamp position between -10 and 10
            if (newPos > 10) newPos = 10
            if (newPos < -10) newPos = -10

            val updates = mutableMapOf<String, Any>("ballPosition" to newPos)

            // Check Win Condition
            if (newPos >= 10) { updates["status"] = "finished"; updates["winnerId"] = player1Id ?: "" }
            if (newPos <= -10) { updates["status"] = "finished"; updates["winnerId"] = "opponent" }

            transaction.update(db.collection("pvp_rooms").document(roomId), updates)
        }
    }

    // Flow 9.0: Bot Logic Injection
    RhythmBotLogic(
        roomId = roomId,
        status = status,
        isPlayer1 = isPlayer1,
        isBotGame = isBotGame
    )

    // Flow 10.0: UI Construction
    Scaffold(
        containerColor = DarkBackground1, // Variable - Color - Dark Background
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rhythm Master", color = TextWhite, fontWeight = FontWeight.Bold) },
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
            // Flow 10.1: Gameplay UI
            if (status == "playing") {

                Text("PUSH THE BALL!", fontWeight = FontWeight.Bold, color = PurpleAccent)
                Spacer(Modifier.height(8.dp))

                // UI - Tug of War Bar
                LinearProgressIndicator(
                    progress = { (ballPosition + 10) / 20f },
                    modifier = Modifier.fillMaxWidth().height(12.dp).networkDropShadow(),
                    color = if(isPlayer1) PurpleAccent else ErrorRed,
                    trackColor = CardColor1
                )

                Spacer(Modifier.height(40.dp))

                Text("Tap when note hits the slot!", color = Color.Gray)
                Spacer(Modifier.height(16.dp))

                // UI - Rhythm Track Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(CardColor1, RoundedCornerShape(12.dp))
                ) {

                    // UI - Fixed Slots (Targets)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.weight(0.05f)) // Start Padding

                        // Slot 1
                        SlotBox(Modifier.weight(0.15f))
                        Spacer(Modifier.weight(0.10f)) // Gap

                        // Slot 2
                        SlotBox(Modifier.weight(0.15f))
                        Spacer(Modifier.weight(0.10f)) // Gap

                        // Slot 3
                        SlotBox(Modifier.weight(0.15f))
                        Spacer(Modifier.weight(0.10f)) // Gap

                        // Slot 4
                        SlotBox(Modifier.weight(0.15f))

                        Spacer(Modifier.weight(0.05f)) // End Padding
                    }


                    // UI - Moving Note
                    // progress 0f -> 1f maps to horizontalBias -1f -> 1f
                    val bias = (progress * 2) - 1

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Note",
                            tint = PurpleAccent,
                            modifier = Modifier
                                .align(BiasAlignment(bias, 0f))
                                .size(40.dp)
                                .background(TextWhite, CircleShape)
                                .border(2.dp, PurpleAccent, CircleShape)
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))

                // UI - Feedback & Combo
                Text(
                    feedbackText,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if(feedbackText.contains("MISS")) ErrorRed else SuccessGreen
                )
                if (combo > 1) {
                    Text("$combo Combo!", fontSize = 24.sp, color = ComboYellow, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.weight(1f))

                // UI - Tap Button
                Button(
                    onClick = { onTap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("TAP HERE!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                }

                Spacer(Modifier.height(20.dp))

            }
            // Flow 10.2: Game Over UI
            else if (status == "finished") {

                Spacer(Modifier.height(40.dp))

                // Logic - Determine Winner
                val iWon = (ballPosition >= 10 && isPlayer1) || (ballPosition <= -10 && !isPlayer1)

                Text(
                    if (iWon) "YOU WON! 🏆" else "YOU LOST 💀",
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
            // Flow 10.3: Loading UI
            else {
                CircularProgressIndicator(color = PurpleAccent)
                Text("Waiting for opponent...", color = TextWhite, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}

// Function - UI Component - The Target Slot
@Composable
fun SlotBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight(0.8f)
            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), // Slight transparency
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.size(8.dp).background(Color.Gray.copy(alpha = 0.5f), CircleShape))
    }
}

// Extension - Utility - Placeholder for custom shadow modifier
fun Modifier.networkDropShadow() = this