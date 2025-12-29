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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RhythmGameScreen(
    roomId: String,
    songRepository: SongRepository,
    onNavigateBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val myId = currentUser?.uid ?: ""
    val context = LocalContext.current

    var roomData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var feedbackText by remember { mutableStateOf("") } // Perfect / Miss
    var combo by remember { mutableStateOf(0) }

    val player1Id = roomData?.get("player1Id") as? String
    val ballPosition = (roomData?.get("ballPosition") as? Long)?.toInt() ?: 0
    val isPlayer1 = myId == player1Id
    val songUrl = roomData?.get("currentSongUrl") as? String
    val status = roomData?.get("status") as? String ?: "waiting"


    val infiniteTransition = rememberInfiniteTransition(label = "rhythm")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing), // 线性移动
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )


    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }


    val handleExit = {
        if (player1Id == myId) db.collection("pvp_rooms").document(roomId).delete()
        else db.collection("pvp_rooms").document(roomId).update("player2Id", null)
        onNavigateBack()
    }
    BackHandler { handleExit() }


    LaunchedEffect(roomId) {
        db.collection("pvp_rooms").document(roomId).addSnapshotListener { s, _ ->
            if (s != null && s.exists()) roomData = s.data else onNavigateBack()
        }
    }


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
        }else if (status == "finished") {

            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }


    // Slot 1: 0.05 - 0.20
    // Gap 1: 0.20 - 0.30
    // Slot 2: 0.30 - 0.45
    // Gap 2: 0.45 - 0.55
    // Slot 3: 0.55 - 0.70
    // Gap 3: 0.70 - 0.80
    // Slot 4: 0.80 - 0.95
    fun checkHit(): Boolean {
        val p = progress
        return (p in 0.05..0.20) || (p in 0.30..0.45) || (p in 0.55..0.70) || (p in 0.80..0.95)
    }

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

        db.runTransaction { transaction ->
            val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
            val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0


            val playerDirection = if (isPlayer1) 1 else -1


            val actualMove = playerDirection * moveAmount

            var newPos = currentPos + actualMove


            if (newPos > 10) newPos = 10
            if (newPos < -10) newPos = -10

            val updates = mutableMapOf<String, Any>("ballPosition" to newPos)


            if (newPos >= 10) { updates["status"] = "finished"; updates["winnerId"] = player1Id ?: "" }
            if (newPos <= -10) { updates["status"] = "finished"; updates["winnerId"] = "opponent" }

            transaction.update(db.collection("pvp_rooms").document(roomId), updates)

        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Rhythm Master") }) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (status == "playing") {

                Text("PUSH THE BALL!", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (ballPosition + 10) / 20f },
                    modifier = Modifier.fillMaxWidth().height(12.dp).networkDropShadow(),
                    color = if(isPlayer1) Color.Blue else Color.Red
                )

                Spacer(Modifier.height(40.dp))


                Text("Tap when note hits the slot!", color = Color.Gray)
                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {


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
                            tint = Color.Magenta,
                            modifier = Modifier
                                .align(BiasAlignment(bias, 0f))
                                .size(40.dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Color.Magenta, CircleShape)
                                .padding(4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))


                Text(feedbackText, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = if(feedbackText.contains("MISS")) Color.Gray else Color.Green)
                if (combo > 1) Text("$combo Combo!", fontSize = 24.sp, color = Color.Yellow)

                Spacer(Modifier.weight(1f))


                Button(
                    onClick = { onTap() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("TAP HERE!", fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(20.dp))

            } else if (status == "finished") {

                Spacer(Modifier.height(40.dp))


                val iWon = (ballPosition >= 10 && isPlayer1) || (ballPosition <= -10 && !isPlayer1)

                Text(if (iWon) "YOU WON! 🏆" else "YOU LOST 💀", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = if(iWon) Color.Green else Color.Red)

                Spacer(Modifier.height(20.dp))
                Button(onClick = handleExit) { Text("Back to Lobby") }
            } else {
                CircularProgressIndicator()
                Text("Waiting for opponent...")
            }
        }
    }
}

@Composable
fun SlotBox(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight(0.8f)
            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {

        Box(Modifier.size(8.dp).background(Color.Gray.copy(alpha = 0.5f), CircleShape))
    }
}


fun Modifier.networkDropShadow() = this 