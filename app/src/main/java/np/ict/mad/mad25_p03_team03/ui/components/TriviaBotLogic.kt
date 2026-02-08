package np.ict.mad.mad25_p03_team03.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlin.random.Random

// Function - Logic Component - AI Bot for Trivia Game
// Flow 1.0: Component Entry Point
@Composable
fun TriviaBotLogic(
    roomId: String,             // Variable - Input - The ID of the room where the bot is playing
    status: String,             // Variable - Input - Current game status ("playing", "waiting", etc.)
    isPlayer1: Boolean,         // Variable - Input - Boolean to check if the current user is the host
    isBotGame: Boolean,         // Variable - Input - Boolean to confirm if this is a PvE (Bot) match
    currentQuestionIndex: Int   // Variable - Input - The current question number being played
) {
    // Flow 1.1: Dependency Setup
    // Initialize Firestore instance to update game state
    val db = FirebaseFirestore.getInstance()

    // Flow 2.0: Bot Activation Logic
    // Triggered whenever 'currentQuestionIndex', 'status', or 'isBotGame' changes
    LaunchedEffect(currentQuestionIndex, status, isBotGame) {
        // Flow 2.1: Logging
        // Debug log to trace when the effect is triggered and with what values
        Log.d("BotLogic", "Effect triggered: Idx=$currentQuestionIndex, Status=$status, IsBot=$isBotGame, IsP1=$isPlayer1")

        // Flow 2.2: Validation Check
        // The bot only runs if:
        // 1. Game is currently "playing"
        // 2. The user running this code is Player 1 (Host) - prevents double bot logic
        // 3. The opponent is actually a Bot
        if (status == "playing" && isPlayer1 && isBotGame) {

            // Flow 2.3: Thinking Simulation
            Log.d("BotLogic", "Bot is thinking...")

            // Logic - Random Delay
            // Bot waits between 5 to 10 seconds before answering to simulate human thought
            val delayTime = Random.nextLong(5000, 10000)
            delay(delayTime)

            // Flow 3.0: Answer Execution
            // Re-check status after delay to ensure game hasn't ended or paused
            if (status == "playing") {

                // Flow 3.1: Atomic Transaction
                // Use a transaction to safely check if the round is still active before answering
                db.runTransaction { transaction ->
                    // Flow 3.2: Fetch Current State
                    val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))

                    // Logic - Check Round Status
                    val roundWinner = snapshot.getString("roundWinnerId")

                    // Only proceed if no one has won this round yet
                    if (roundWinner == null) {
                        Log.d("BotLogic", "Bot is answering!")

                        // Variable - State - Get current ball position
                        val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

                        // Flow 3.3: Dynamic Difficulty
                        // Default accuracy is 70%
                        var accuracy = 70
                        // Logic - If Player 1 is winning (Ball > 0), Bot tries harder (90%)
                        if (currentPos > 0) accuracy = 90
                        // Logic - If Bot is winning (Ball < 0), Bot relaxes (50%)
                        if (currentPos < 0) accuracy = 50

                        // Flow 3.4: Determine Answer Outcome
                        // Roll a random number (0-99). If less than accuracy, Bot gets it right.
                        val isCorrect = Random.nextInt(100) < accuracy

                        if (isCorrect) {
                            // Flow 3.5: Calculate New Position
                            // Bot is Player 2, so it moves ball towards -2
                            var newPos = currentPos - 1

                            // Logic - Clamp values
                            if (newPos > 2) newPos = 2
                            if (newPos < -2) newPos = -2

                            // Variable - Map - Prepare update data
                            val updates = mutableMapOf<String, Any>(
                                "roundWinnerId" to "BOT",
                                "ballPosition" to newPos
                            )

                            // Flow 3.6: Check Win/Loss
                            // If ball hits -2 -> Bot Wins the Game
                            if (newPos <= -2) {
                                updates["status"] = "finished"
                                updates["winnerId"] = "opponent"
                            }
                            // If ball hits 2 -> Player 1 Wins (Unlikely here, but handled for safety)
                            else if (newPos >= 2) {
                                updates["status"] = "finished"
                                updates["winnerId"] = snapshot.getString("player1Id") ?: ""
                            }

                            // Flow 3.7: Commit Update
                            transaction.update(db.collection("pvp_rooms").document(roomId), updates)
                        } else {
                            // Logic - Bot Missed
                            Log.d("BotLogic", "Bot decided to miss (RNG)")
                        }
                    } else {
                        // Logic - Round Over
                        Log.d("BotLogic", "Round already won by: $roundWinner")
                    }
                }.addOnFailureListener { e ->
                    // Flow 4.0: Error Handling
                    Log.e("BotLogic", "Transaction failed", e)
                }
            }
        }
    }
}