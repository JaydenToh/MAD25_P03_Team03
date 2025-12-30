package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 专门处理 Trivia (猜歌) 模式下的 Bot 行为
 * 只有房主 (Player 1) 会调用此组件
 */
@Composable
fun TriviaBotLogic(
    roomId: String,
    status: String,
    isPlayer1: Boolean,
    isBotGame: Boolean,
    currentQuestionIndex: Int,
    onBotAction: (String) -> Unit = {} // 可选：通知 UI Bot 做了什么
) {
    val db = FirebaseFirestore.getInstance()

    // 监听：当题目更新 (currentQuestionIndex) 或 状态改变 (status) 时触发
    LaunchedEffect(currentQuestionIndex, status) {
        // 核心判断：只有 游戏进行中 + 我是房主 + 这是人机局 才运行
        if (status == "playing" && isPlayer1 && isBotGame) {

            // 1. 模拟思考时间 (1.5秒 - 4.5秒)
            val delayTime = Random.nextLong(1500, 4500)
            delay(delayTime)

            // 二次检查：思考完后游戏是否还在继续？
            if (status == "playing") {
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))

                    // 只有当这一轮还没人赢 (roundWinnerId == null) 时 Bot 才出手
                    if (snapshot.getString("roundWinnerId") == null) {
                        val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

                        // 2. 动态难度 (橡皮筋机制)
                        // 球越靠近 Bot (-2), Bot 越强; 球越靠近 Player (+2), Bot 越弱
                        // currentPos: 正数(Player优势), 负数(Bot优势)
                        var accuracy = 80 // 基础胜率 80%

                        if (currentPos > 0) accuracy = 90  // Bot 落后，变强
                        if (currentPos < 0) accuracy = 60  // Bot 领先，变弱 (给玩家机会)

                        val isCorrect = Random.nextInt(100) < accuracy

                        if (isCorrect) {
                            // Bot (Player 2) 答对 -> 往负方向推 (-1)
                            var newPos = currentPos - 1

                            // 限制范围
                            if (newPos > 2) newPos = 2
                            if (newPos < -2) newPos = -2

                            val updates = mutableMapOf<String, Any>(
                                "roundWinnerId" to "BOT",
                                "ballPosition" to newPos
                            )

                            // 判赢
                            if (newPos <= -2) {
                                updates["status"] = "finished"
                                updates["winnerId"] = "opponent" // Bot 赢
                            } else if (newPos >= 2) {
                                // 防御性代码：虽然 Bot 不会推向 +2，但以防万一
                                updates["status"] = "finished"
                                updates["winnerId"] = snapshot.getString("player1Id") ?: ""
                            }

                            transaction.update(db.collection("pvp_rooms").document(roomId), updates)

                            // 可以在这里打 Log 或者调用回调
                            // onBotAction("Bot answered correctly")
                        } else {
                            // Bot 答错：什么都不做，或者你可以加个 "Bot Missed" 的状态让 UI 显示
                        }
                    }
                }
            }
        }
    }
}