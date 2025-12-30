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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PvpGameScreen(
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
    var questions by remember { mutableStateOf<List<SongQuestion>>(emptyList()) }
    var message by remember { mutableStateOf("Waiting for opponent...") }

    val player2Id = roomData?.get("player2Id") as? String

    val isBotGame = player2Id == "BOT" || player2Id == "AI" // 根据你存的值

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    val currentIdx = (roomData?.get("currentQuestionIndex") as? Long)?.toInt() ?: 0


    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }


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
                setOnPreparedListener {
                    start()
                }
                setOnErrorListener { _, _, _ ->
                    true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


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


    val player1Id = roomData?.get("player1Id") as? String
    val status = roomData?.get("status") as? String ?: "waiting"


    // 0 = Center
    // Positive (+) = Towards Player 2
    // Negative (-) = Towards Player 1
    val ballPosition = (roomData?.get("ballPosition") as? Long)?.toInt() ?: 0


    val isPlayer1 = myId == player1Id


    val handleExit = {
        if (player1Id == myId) {
            db.collection("pvp_rooms").document(roomId).delete()
        } else {
            if (status == "waiting" || status == "playing") {
                db.collection("pvp_rooms").document(roomId).update(
                    mapOf("player2Id" to null, "status" to "waiting", "ballPosition" to 0) // 退出重置位置
                )
            }
        }
        onNavigateBack()
    }

    BackHandler { handleExit() }


    LaunchedEffect(roomId) {
        val docRef = db.collection("pvp_rooms").document(roomId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                roomData = snapshot.data
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

    LaunchedEffect(currentIdx, status, questions) {

        if (status == "playing" && questions.isNotEmpty()) {
            val currentQuestion = questions.getOrNull(currentIdx)
            val url = currentQuestion?.audioUrl

            if (!url.isNullOrEmpty()) {

                delay(300)
                playAudio(url)
            }
        } else {

            stopAudio()
        }
    }


    LaunchedEffect(roomData) {
        val p1Id = roomData?.get("player1Id") as? String
        val questionsInRoom = roomData?.get("questions") as? List<*>
        if (p1Id == myId && (questionsInRoom == null || questionsInRoom.isEmpty())) {
            val songs = songRepository.fetchSongsFromSupabase(GameMode.ENGLISH).take(10)
            val mappedQuestions = songs.map { song ->
                val options = (listOf(song.title) + song.fakeOptions).shuffled().take(4)
                mapOf("correctTitle" to song.title, "options" to options, "audioUrl" to song.audioUrl)
            }

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


    fun submitAnswer(selectedOption: String) {
        if (status != "playing" || roundWinnerId != null || gameWinnerId != null) return
        val currentQuestion = questions.getOrNull(currentIdx) ?: return

        if (selectedOption == currentQuestion.correctTitle) {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))

                if (snapshot.getString("roundWinnerId") == null) {
                    val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0


                    var newPos = if (isPlayer1) currentPos + 1 else currentPos - 1


                    if (newPos > 3) newPos = 3
                    if (newPos < -3) newPos = -3

                    val updates = mutableMapOf<String, Any>(
                        "roundWinnerId" to myId,
                        "ballPosition" to newPos
                    )


                    if (newPos == 3) {

                        updates["winnerId"] = player1Id ?: "" // P1 ID
                        updates["status"] = "finished"
                    } else if (newPos == -3) {

                        updates["winnerId"] = if (isPlayer1) "opponent" else myId
                        updates["status"] = "finished"
                    }

                    transaction.update(
                        db.collection("pvp_rooms").document(roomId),
                        updates
                    )
                }
            }
        } else {
            message = "Wrong answer! 😱"

        }
    }


    LaunchedEffect(roundWinnerId) {
        if (roundWinnerId != null) {
            message = if (roundWinnerId == myId) "💪 PUSHED!" else "🛡️ PUSHED BACK!"
            delay(1500)


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

    TriviaBotLogic(
        roomId = roomId,
        status = status,
        isPlayer1 = isPlayer1,
        isBotGame = isBotGame,
        currentQuestionIndex = currentIdx
    )

    Scaffold(
        topBar = {
            if (status == "waiting" || status == "playing") {
                CenterAlignedTopAppBar(
                    title = { Text("Tug of War PVP") },
                    navigationIcon = {
                        IconButton(onClick = { handleExit() }) { Text("❌", fontSize = 18.sp) }
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (status == "waiting") {
                CircularProgressIndicator()
                Text("Waiting for opponent...", modifier = Modifier.padding(top = 16.dp))
            } else if (status == "playing") {



                Spacer(Modifier.height(16.dp))

                BallTrackUI(ballPosition = ballPosition, isPlayer1 = isPlayer1)

                Spacer(Modifier.height(24.dp))


                val question = questions.getOrNull(currentIdx)
                if (question != null) {
                    Text("Question ${currentIdx + 1}", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { val url = question.audioUrl
                            if (!url.isNullOrEmpty()) {
                                playAudio(url)
                            } else {
                                Toast.makeText(context, "No audio available", Toast.LENGTH_SHORT).show()
                            } },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("▶️ Replay Song")
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(Modifier.height(16.dp))

                    question.options.forEach { option ->
                        Button(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            enabled = roundWinnerId == null,
                            onClick = { submitAnswer(option) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (roundWinnerId == null) MaterialTheme.colorScheme.primaryContainer else Color.Gray
                            )
                        ) {
                            Text(option, fontSize = 18.sp)
                        }
                    }
                }
            } else {
                // --- Game Over  ---
                Spacer(Modifier.height(40.dp))
                Text("GAME OVER", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(24.dp))

                // ballPosition == 3 -> P1
                // ballPosition == -3 -> P2

                val didIWin = if (isPlayer1) (ballPosition >= 3) else (ballPosition <= -3)

                if (didIWin) {
                    Text("🏆 YOU WON!", style = MaterialTheme.typography.displayMedium, color = Color(0xFF4CAF50))
                    Text("You smashed them!", style = MaterialTheme.typography.bodyLarge)
                } else {
                    Text("💀 YOU LOST", style = MaterialTheme.typography.displayMedium, color = Color.Red)
                    Text("Crushed by the ball...", style = MaterialTheme.typography.bodyLarge)
                }

                Spacer(Modifier.height(48.dp))
                Button(
                    onClick = { handleExit() },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Back to Lobby")
                }
            }
        }
    }
}


@Composable
fun BallTrackUI(ballPosition: Int, isPlayer1: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isPlayer1) "YOU" else "ENEMY",
                fontWeight = FontWeight.Bold,
                color = if (isPlayer1) Color.Blue else Color.Red
            )
            Text(
                text = if (isPlayer1) "ENEMY" else "YOU",
                fontWeight = FontWeight.Bold,
                color = if (isPlayer1) Color.Red else Color.Blue
            )
        }

        Spacer(Modifier.height(8.dp))


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

                Divider(
                    modifier = Modifier.align(Alignment.Center),
                    thickness = 4.dp,
                    color = Color.LightGray
                )

                //  (-2, -1, 0, 1, 2)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Gray, CircleShape)
                        )
                    }
                }


                // BiasAlignment
                if (ballPosition in -2..2) {
                    // 将 -2..2 映射到 -1f..1f
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
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💣", fontSize = 18.sp)
                        }
                    }
                }
            }


            Text("👤", fontSize = 24.sp)
        }


        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(if (ballPosition <= -3) "💥 CRUSHED!" else "", color = Color.Red, fontWeight = FontWeight.Bold)

            Text(if (ballPosition >= 3) "💥 CRUSHED!" else "", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}