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

        if (status == "playing" && isPlayer1 && isBotGame) {

            Log.d("Bot", "Rhythm Bot Started")


            while (true) {

                val interval = Random.nextLong(480, 520)
                delay(interval)



                db.runTransaction { transaction ->
                    val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
                    val currentStatus = snapshot.getString("status")

                    if (currentStatus == "playing") {
                        val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0


                        var accuracy = 40


                        if (currentPos > 2) accuracy = 50
                        if (currentPos > 5) accuracy = 70


                        if (currentPos < -2) accuracy = 40
                        if (currentPos < -5) accuracy = 30

                        val isPerfect = Random.nextInt(100) < accuracy


                        val move = if (isPerfect) -1 else 1
                        var newPos = currentPos + move


                        if (newPos > 10) newPos = 10
                        if (newPos < -10) newPos = -10

                        val updates = mutableMapOf<String, Any>("ballPosition" to newPos)


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