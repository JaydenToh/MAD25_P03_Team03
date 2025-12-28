package np.ict.mad.mad25_p03_team03.ui

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState // 用于动画
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

    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    // ✅ 页面关闭时释放资源
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // ✅ 播放函数
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
                setOnErrorListener { _, _, _ ->
                    Toast.makeText(context, "Audio Error", Toast.LENGTH_SHORT).show()
                    true
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load audio", Toast.LENGTH_SHORT).show()
        }
    }

    // 解析基础数据
    val player1Id = roomData?.get("player1Id") as? String
    val status = roomData?.get("status") as? String ?: "waiting"

    // 🔥 核心：获取铅球位置 (默认为 0)
    // 0 = Center
    // Positive (+) = Towards Player 2
    // Negative (-) = Towards Player 1
    val ballPosition = (roomData?.get("ballPosition") as? Long)?.toInt() ?: 0

    // 判断我是 P1 还是 P2
    val isPlayer1 = myId == player1Id

    // 退出逻辑 (保持不变)
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

    // 监听房间数据
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

    // 房主生成题目 (保持不变)
    LaunchedEffect(roomData) {
        val p1Id = roomData?.get("player1Id") as? String
        val questionsInRoom = roomData?.get("questions") as? List<*>
        if (p1Id == myId && (questionsInRoom == null || questionsInRoom.isEmpty())) {
            val songs = songRepository.fetchSongsFromSupabase(GameMode.ENGLISH).take(10) // 取多一点题目
            val mappedQuestions = songs.map { song ->
                val options = (listOf(song.title) + song.fakeOptions).shuffled().take(4)
                mapOf("correctTitle" to song.title, "options" to options, "audioUrl" to song.audioUrl)
            }
            // 初始化 ballPosition 为 0
            db.collection("pvp_rooms").document(roomId).update(
                mapOf(
                    "questions" to mappedQuestions,
                    "ballPosition" to 0
                )
            )
        }
    }

    val currentIdx = (roomData?.get("currentQuestionIndex") as? Long)?.toInt() ?: 0
    val roundWinnerId = roomData?.get("roundWinnerId") as? String
    // 这里的 winnerId 是整场游戏的赢家
    val gameWinnerId = roomData?.get("winnerId") as? String

    // 🔥 提交答案逻辑 (修改为推球)
    fun submitAnswer(selectedOption: String) {
        if (status != "playing" || roundWinnerId != null || gameWinnerId != null) return
        val currentQuestion = questions.getOrNull(currentIdx) ?: return

        if (selectedOption == currentQuestion.correctTitle) {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
                // 只有这一轮还没人赢的时候才处理
                if (snapshot.getString("roundWinnerId") == null) {
                    val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

                    // 逻辑：P1 答对 +1 (向右推), P2 答对 -1 (向左推)
                    var newPos = if (isPlayer1) currentPos + 1 else currentPos - 1

                    // 限制范围 (虽然 UI 上只有 +/-3，但防止溢出)
                    if (newPos > 3) newPos = 3
                    if (newPos < -3) newPos = -3

                    val updates = mutableMapOf<String, Any>(
                        "roundWinnerId" to myId,
                        "ballPosition" to newPos
                    )

                    // 检查是否结束游戏 (砸到人了)
                    if (newPos == 3) {
                        // 到了 +3，说明 P1 把球推到了 P2 脸上 -> P1 赢
                        updates["winnerId"] = player1Id ?: "" // P1 ID
                        updates["status"] = "finished"
                    } else if (newPos == -3) {
                        // 到了 -3，说明 P2 把球推到了 P1 脸上 -> P2 赢
                        // 这里需要获取 P2 ID，简单起见我们如果不存 P2 ID，可以用 !player1Id 判断
                        // 但最好存了 player2Id。这里假设 'status' 变成 finished 就能在 UI 处理
                        updates["winnerId"] = if (isPlayer1) "opponent" else myId // 逻辑稍微复杂，直接在 UI 判分
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
            // 惩罚机制：答错可以冻结几秒，或者球反向滚 (太残忍了，先不加)
        }
    }

    // 回合过渡逻辑
    LaunchedEffect(roundWinnerId) {
        if (roundWinnerId != null) {
            message = if (roundWinnerId == myId) "💪 PUSHED!" else "🛡️ PUSHED BACK!"
            delay(1500)

            // 只有房主负责切题，且如果没有人赢才切题
            if (player1Id == myId && gameWinnerId == null) {
                if (currentIdx + 1 < questions.size) {
                    db.collection("pvp_rooms").document(roomId).update(
                        mapOf("currentQuestionIndex" to currentIdx + 1, "roundWinnerId" to null)
                    )
                } else {
                    // 题目用完了但还没分胜负？平局或者根据位置判
                    db.collection("pvp_rooms").document(roomId).update("status", "finished")
                }
            }
        } else {
            message = "Push the ball to enemy! 💣"
        }
    }

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

                // --- 🔥 这里的 UI 是重点：铅球轨道 ---
                // P1 在左 (-3), P2 在右 (+3)
                // 格子: [-2] [-1] [0] [+1] [+2]

                Spacer(Modifier.height(16.dp))

                BallTrackUI(ballPosition = ballPosition, isPlayer1 = isPlayer1)

                Spacer(Modifier.height(24.dp))

                // 题目显示区域
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
                        Text("▶️ Play Song")
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
                // --- Game Over 结算 ---
                Spacer(Modifier.height(40.dp))
                Text("GAME OVER", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(24.dp))

                // 判断赢家
                // ballPosition == 3 -> P1 赢
                // ballPosition == -3 -> P2 赢

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

// 🔥 新增组件：铅球轨道 UI
@Composable
fun BallTrackUI(ballPosition: Int, isPlayer1: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 顶部文字指示
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

        // 轨道展示
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧用户图标
            Text("👤", fontSize = 24.sp)

            // 轨道主体
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .padding(horizontal = 8.dp)
            ) {
                // 背景轨道线
                Divider(
                    modifier = Modifier.align(Alignment.Center),
                    thickness = 4.dp,
                    color = Color.LightGray
                )

                // 轨道上的 5 个刻度点 (-2, -1, 0, 1, 2)
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

                // 💣 铅球 (根据 ballPosition 移动)
                // 映射逻辑：ballPosition 从 -3 到 3
                // 我们在轨道上只显示 -2 到 2 的位置
                // BiasAlignment 的 horizontalBias 范围是 -1f (最左) 到 1f (最右)
                if (ballPosition in -2..2) {
                    // 将 -2..2 映射到 -1f..1f
                    val hBias = ballPosition / 2f

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                    ) {
                        // ✅ 修复：使用 BiasAlignment 类而不是 Alignment 接口
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

            // 右侧用户图标
            Text("👤", fontSize = 24.sp)
        }

        // 爆炸效果提示 (当位置达到 +/- 3 时)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 如果球撞到了左边 (P1 输)
            Text(if (ballPosition <= -3) "💥 CRUSHED!" else "", color = Color.Red, fontWeight = FontWeight.Bold)
            // 如果球撞到了右边 (P2 输)
            Text(if (ballPosition >= 3) "💥 CRUSHED!" else "", color = Color.Red, fontWeight = FontWeight.Bold)
        }
    }
}