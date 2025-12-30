package np.ict.mad.mad25_p03_team03.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun TriviaBotLogic(
    roomId: String,
    status: String,
    isPlayer1: Boolean,
    isBotGame: Boolean,
    currentQuestionIndex: Int
) {
    val db = FirebaseFirestore.getInstance()

    // 🔥 核心修复：把 isBotGame 加入到监听键值里
    // 这样当 Firestore 数据加载完成，isBotGame 变成 true 时，这个逻辑会重启
    LaunchedEffect(currentQuestionIndex, status, isBotGame) {
        Log.d("BotLogic", "Effect triggered: Idx=$currentQuestionIndex, Status=$status, IsBot=$isBotGame, IsP1=$isPlayer1")

        // 只有 游戏进行中 + 我是房主 + 这是人机局 才运行
        if (status == "playing" && isPlayer1 && isBotGame) {
            Log.d("BotLogic", "Bot is thinking...")

            // 1. 模拟思考时间
            val delayTime = Random.nextLong(5000, 10000)
            delay(delayTime)

            // 二次检查状态
            if (status == "playing") {
                db.runTransaction { transaction ->
                    val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))

                    // 检查这一轮是否已经有人赢了
                    val roundWinner = snapshot.getString("roundWinnerId")
                    if (roundWinner == null) {
                        Log.d("BotLogic", "Bot is answering!")

                        val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

                        // 2. 动态难度
                        var accuracy = 70 // 基础胜率
                        if (currentPos > 0) accuracy = 90  // Bot 落后，变强
                        if (currentPos < 0) accuracy = 50  // Bot 领先，变弱

                        val isCorrect = Random.nextInt(100) < accuracy

                        if (isCorrect) {
                            var newPos = currentPos - 1
                            if (newPos > 2) newPos = 2
                            if (newPos < -2) newPos = -2

                            val updates = mutableMapOf<String, Any>(
                                "roundWinnerId" to "BOT",
                                "ballPosition" to newPos
                            )

                            if (newPos <= -2) {
                                updates["status"] = "finished"
                                updates["winnerId"] = "opponent"
                            } else if (newPos >= 2) {
                                updates["status"] = "finished"
                                updates["winnerId"] = snapshot.getString("player1Id") ?: ""
                            }

                            transaction.update(db.collection("pvp_rooms").document(roomId), updates)
                        } else {
                            Log.d("BotLogic", "Bot decided to miss (RNG)")
                        }
                    } else {
                        Log.d("BotLogic", "Round already won by: $roundWinner")
                    }
                }.addOnFailureListener { e ->
                    Log.e("BotLogic", "Transaction failed", e)
                }
            }
        }
    }
}