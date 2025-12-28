package np.ict.mad.mad25_p03_team03.ui

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.google.firebase.firestore.SetOptions
import np.ict.mad.mad25_p03_team03.data.SongRepository
import kotlinx.coroutines.delay
import np.ict.mad.mad25_p03_team03.data.GameMode

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

    var roomData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var questions by remember { mutableStateOf<List<SongQuestion>>(emptyList()) }
    var message by remember { mutableStateOf("Waiting for opponent...") }

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
            }
        }
    }


    LaunchedEffect(roomData) {
        val player1Id = roomData?.get("player1Id") as? String
        val questionsInRoom = roomData?.get("questions") as? List<*>

        if (player1Id == myId && (questionsInRoom == null || questionsInRoom.isEmpty())) {
            val songs = songRepository.fetchSongsFromSupabase(GameMode.ENGLISH).take(5)

            val mappedQuestions = songs.map { song ->

                val options = (listOf(song.title) + song.fakeOptions).shuffled().take(4)


                mapOf(
                    "correctTitle" to song.title,
                    "options" to options,
                    "audioUrl" to song.audioUrl
                )
            }


            db.collection("pvp_rooms").document(roomId)
                .update("questions", mappedQuestions)
        }
    }


    val status = roomData?.get("status") as? String ?: "waiting"
    val currentIdx = (roomData?.get("currentQuestionIndex") as? Long)?.toInt() ?: 0
    val scores = roomData?.get("scores") as? Map<String, Long> ?: emptyMap()
    val roundWinnerId = roomData?.get("roundWinnerId") as? String

    val myScore = scores[myId]?.toInt() ?: 0

    val opponentScore = scores.entries.find { it.key != myId }?.value?.toInt() ?: 0


    fun submitAnswer(selectedOption: String) {
        if (status != "playing" || roundWinnerId != null) return

        val currentQuestion = questions.getOrNull(currentIdx) ?: return

        if (selectedOption == currentQuestion.correctTitle) {

            db.runTransaction { transaction ->
                val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
                val currentWinner = snapshot.getString("roundWinnerId")


                if (currentWinner == null) {
                    transaction.update(
                        db.collection("pvp_rooms").document(roomId),
                        mapOf(
                            "roundWinnerId" to myId,
                            "scores.$myId" to myScore + 10 // 加分
                        )
                    )
                }
            }.addOnSuccessListener {

            }
        } else {

            message = "Wrong answer! 😱"
        }
    }


    LaunchedEffect(roundWinnerId) {
        if (roundWinnerId != null) {

            message = if (roundWinnerId == myId) "🎉 You were faster!" else "⚡ Opponent snatched it!"


            delay(2000)


            if (roomData?.get("player1Id") == myId) {
                if (currentIdx + 1 < questions.size) {
                    db.collection("pvp_rooms").document(roomId).update(
                        mapOf(
                            "currentQuestionIndex" to currentIdx + 1,
                            "roundWinnerId" to null
                        )
                    )
                } else {

                    db.collection("pvp_rooms").document(roomId).update("status", "finished")
                }
            }
        } else {
            message = "Who is faster? 🚀"
        }
    }


    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (status == "waiting") {
                CircularProgressIndicator()
                Text("Waiting for opponent...", modifier = Modifier.padding(top = 16.dp))
            } else if (status == "playing") {

                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Me", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("$myScore", style = MaterialTheme.typography.displaySmall)
                    }
                    Text("VS", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.CenterVertically))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Opponent", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Text("$opponentScore", style = MaterialTheme.typography.displaySmall)
                    }
                }


                val question = questions.getOrNull(currentIdx)
                if (question != null) {
                    Text("Question ${currentIdx + 1}", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))


                    Button(onClick = { /* playAudio logic */ }) {
                        Text("▶️ Play Song")
                    }

                    Spacer(Modifier.height(20.dp))


                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (roundWinnerId == myId) Color.Green else if (roundWinnerId != null) Color.Red else Color.Black,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(20.dp))


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
                Text("Game Over!", style = MaterialTheme.typography.headlineLarge)
                val winnerText = if (myScore > opponentScore) "You Won! 🏆" else "You Lost 😢"
                Text(winnerText, style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onNavigateBack) { Text("Back to Home") }
            }
        }
    }
}