package np.ict.mad.mad25_p03_team03.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun RhythmBotLogic(
    roomId: String,
    status: String,
    isPlayer1: Boolean,
    isBotGame: Boolean
) {
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(status, isBotGame) {
        // 只有 游戏进行中 + 我是房主 + 人机局 才运行
        if (status == "playing" && isPlayer1 && isBotGame) {

            Log.d("Bot", "Rhythm Bot Started")

            // 死循环，模拟玩家不断点击
            while (true) {
                // 1. 节奏间隔
                // 你的动画是 2000ms 跑一圈，有 4 个 Slot
                // 理论间隔 = 2000 / 4 = 500ms
                // 加一点随机波动 (Humanize)
                val interval = Random.nextLong(480, 520)
                delay(interval)

                // 检查游戏是否还在进行 (防止退出后还在跑)
                // 这里最好重新 fetch status，但简化版直接读上面的 status 参数可能滞后
                // 所以我们在 transaction 里再查一次最稳妥

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
                    val currentStatus = snapshot.getString("status")

                    if (currentStatus == "playing") {
                        val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

                        // 2. 动态难度 (Dynamic Difficulty)
                        // 基础命中率 75%
                        var accuracy = 40

                        // Bot 落后 (Bot在 -10, Player在 +10) -> Bot 需要反击
                        // 注意：Bot 是 Player 2，它的目标是把球推向 -10
                        // 如果 currentPos > 0 (球在玩家那边)，Bot 劣势 -> 变强
                        if (currentPos > 2) accuracy = 50
                        if (currentPos > 5) accuracy = 70

                        // Bot 领先 (球在 Bot 那边 < 0) -> 放水
                        if (currentPos < -2) accuracy = 40
                        if (currentPos < -5) accuracy = 30

                        val isPerfect = Random.nextInt(100) < accuracy

                        // Bot (Player 2) 想要往 -1 (负方向) 推
                        // Perfect: -1
                        // Miss: +1 (反向惩罚)

                        val move = if (isPerfect) -1 else 1
                        var newPos = currentPos + move

                        // 限制范围
                        if (newPos > 10) newPos = 10
                        if (newPos < -10) newPos = -10

                        val updates = mutableMapOf<String, Any>("ballPosition" to newPos)

                        // 判赢
                        if (newPos >= 10) {
                            updates["status"] = "finished"
                            updates["winnerId"] = snapshot.getString("player1Id") ?: ""
                        }
                        if (newPos <= -10) {
                            updates["status"] = "finished"
                            updates["winnerId"] = "opponent"
                        }

                        transaction.update(db.collection("pvp_rooms").document(roomId), updates)
                    }
                }.addOnFailureListener {
                    // Log error if needed
                }
            }
        }
    }
}