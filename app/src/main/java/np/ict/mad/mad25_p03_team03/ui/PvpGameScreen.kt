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

    // 状态管理
    var roomData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var questions by remember { mutableStateOf<List<SongQuestion>>(emptyList()) } // 复用之前的 SongQuestion 类
    var message by remember { mutableStateOf("Waiting for opponent...") }

    LaunchedEffect(roomId) {
        val docRef = db.collection("pvp_rooms").document(roomId)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                roomData = snapshot.data

                // ⬇️⬇️⬇️ 解析题目数据 (这也非常重要，否则看不到题目) ⬇️⬇️⬇️
                val questionsData = snapshot.get("questions") as? List<Map<String, Any>>
                if (questionsData != null && questionsData.isNotEmpty()) {
                    questions = questionsData.map { q ->
                        SongQuestion(
                            correctTitle = q["correctTitle"] as String,
                            options = (q["options"] as List<*>).map { it.toString() }, // 确保转换安全
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

        // 如果我是房主，且房间没题目，且游戏还没结束
        if (player1Id == myId && (questionsInRoom == null || questionsInRoom.isEmpty())) {
            // Fetch 题目
            val songs = songRepository.fetchSongsFromSupabase(GameMode.ENGLISH).take(5) // 取5题

            val mappedQuestions = songs.map { song ->
                // 构造 SongQuestion 对象
                val options = (listOf(song.title) + song.fakeOptions).shuffled().take(4)

                // 转成 Map 存入 Firestore
                mapOf(
                    "correctTitle" to song.title,
                    "options" to options,
                    "audioUrl" to song.audioUrl
                )
            }

            // 上传到房间
            db.collection("pvp_rooms").document(roomId)
                .update("questions", mappedQuestions)
        }
    }

    // 解析房间数据
    val status = roomData?.get("status") as? String ?: "waiting"
    val currentIdx = (roomData?.get("currentQuestionIndex") as? Long)?.toInt() ?: 0
    val scores = roomData?.get("scores") as? Map<String, Long> ?: emptyMap()
    val roundWinnerId = roomData?.get("roundWinnerId") as? String

    val myScore = scores[myId]?.toInt() ?: 0
    // 找出对手的分数 (遍历 map 只要 key 不是我就行)
    val opponentScore = scores.entries.find { it.key != myId }?.value?.toInt() ?: 0

    // 抢答逻辑：提交答案
    fun submitAnswer(selectedOption: String) {
        if (status != "playing" || roundWinnerId != null) return // 这一轮已经有人赢了，或者游戏没开始

        val currentQuestion = questions.getOrNull(currentIdx) ?: return

        if (selectedOption == currentQuestion.correctTitle) {
            // ✅ 答对了！发起事务去“抢”这个分
            db.runTransaction { transaction ->
                val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
                val currentWinner = snapshot.getString("roundWinnerId")

                // 核心：只有当 currentWinner 为空时，我才能赢
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
                // 抢答成功
            }
        } else {
            // ❌ 答错了 (可以做惩罚，比如扣分或者冻结按钮)
            message = "Wrong answer! 😱"
        }
    }

    // 自动跳转下一题逻辑 (由 Player 1 负责控制，避免冲突)
    LaunchedEffect(roundWinnerId) {
        if (roundWinnerId != null) {
            // 显示谁赢了
            message = if (roundWinnerId == myId) "🎉 You were faster!" else "⚡ Opponent snatched it!"

            // 延迟 2 秒进入下一题
            delay(2000)

            // 只有 Player 1 负责写入数据库更新题目，避免两个人同时写
            if (roomData?.get("player1Id") == myId) {
                if (currentIdx + 1 < questions.size) {
                    db.collection("pvp_rooms").document(roomId).update(
                        mapOf(
                            "currentQuestionIndex" to currentIdx + 1,
                            "roundWinnerId" to null // 重置赢家状态
                        )
                    )
                } else {
                    // 游戏结束
                    db.collection("pvp_rooms").document(roomId).update("status", "finished")
                }
            }
        } else {
            message = "Who is faster? 🚀"
        }
    }

    // UI 布局
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (status == "waiting") {
                CircularProgressIndicator()
                Text("Waiting for opponent...", modifier = Modifier.padding(top = 16.dp))
            } else if (status == "playing") {
                // 顶部比分板
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

                // 题目区域
                val question = questions.getOrNull(currentIdx)
                if (question != null) {
                    Text("Question ${currentIdx + 1}", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))

                    // 这里可以复用之前的 playAudio 逻辑
                    Button(onClick = { /* playAudio logic */ }) {
                        Text("▶️ Play Song")
                    }

                    Spacer(Modifier.height(20.dp))

                    // 状态提示
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (roundWinnerId == myId) Color.Green else if (roundWinnerId != null) Color.Red else Color.Black,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(20.dp))

                    // 选项按钮
                    question.options.forEach { option ->
                        Button(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            enabled = roundWinnerId == null, // 如果有人赢了，按钮禁用
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
                // 游戏结束界面
                Text("Game Over!", style = MaterialTheme.typography.headlineLarge)
                val winnerText = if (myScore > opponentScore) "You Won! 🏆" else "You Lost 😢"
                Text(winnerText, style = MaterialTheme.typography.headlineMedium)
                Button(onClick = onNavigateBack) { Text("Back to Home") }
            }
        }
    }
}