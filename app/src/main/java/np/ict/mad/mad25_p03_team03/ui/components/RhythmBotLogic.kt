package np.ict.mad.mad25_p03_team03.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlin.random.Random

// Function - Logic Component - AI Bot for Rhythm Game
// Flow 1.0: Component Entry Point
@Composable
fun RhythmBotLogic(
    roomId: String,      // Variable - Input - The ID of the room where the bot is playing
    status: String,      // Variable - Input - Current game status ("playing", "waiting", etc.)
    isPlayer1: Boolean,  // Variable - Input - Boolean to check if the current user is the host
    isBotGame: Boolean   // Variable - Input - Boolean to confirm if this is a PvE (Bot) match
) {
    // Flow 1.1: Dependency Setup
    // Initialize Firestore instance to update game state
    val db = FirebaseFirestore.getInstance()

    // Flow 2.0: Bot Activation Logic
    // Triggered whenever 'status' or 'isBotGame' changes
    LaunchedEffect(status, isBotGame) {

        // Flow 2.1: Validation Check
        // The bot only runs if:
        // 1. Game is currently "playing"
        // 2. The user running this code is Player 1 (Host) - prevents double bot logic
        // 3. The opponent is actually a Bot
        if (status == "playing" && isPlayer1 && isBotGame) {

            // Flow 2.2: Logging
            // Debug log to confirm bot has started
            Log.d("Bot", "Rhythm Bot Started")


            // Flow 3.0: Game Loop
            // Infinite loop that simulates the bot playing until the game ends
            while (true) {

                // Flow 3.1: Simulate Reaction Time
                // Bot waits between 480ms and 520ms before making a move (approx 2 moves/sec)
                val interval = Random.nextLong(480, 520)
                delay(interval)


                // Flow 4.0: Atomic Move Execution
                // Use a transaction to safely read and update the ball position
                db.runTransaction { transaction ->
                    // Flow 4.1: Fetch Current State
                    // Read the latest room data from Firestore
                    val snapshot = transaction.get(db.collection("pvp_rooms").document(roomId))
                    val currentStatus = snapshot.getString("status")

                    // Flow 4.2: Game Active Check
                    // Only process move if the game is still valid
                    if (currentStatus == "playing") {
                        // Variable - State - Get current ball position (default to 0)
                        val currentPos = snapshot.getLong("ballPosition")?.toInt() ?: 0

                        // Flow 4.3: Dynamic Difficulty Adjustment
                        // Default accuracy is 40%
                        var accuracy = 40

                        // Logic - If Bot is losing (Ball > 2), it tries harder (50%)
                        if (currentPos > 2) accuracy = 50
                        // Logic - If Bot is losing badly (Ball > 5), it tries very hard (70%)
                        if (currentPos > 5) accuracy = 70

                        // Logic - If Bot is winning (Ball < -2), it relaxes (40%)
                        if (currentPos < -2) accuracy = 40
                        // Logic - If Bot is crushing player (Ball < -5), it makes mistakes (30%)
                        if (currentPos < -5) accuracy = 30

                        // Flow 4.4: Determine Move Outcome
                        // Roll a random number (0-99). If less than accuracy, it's a "Perfect" hit.
                        val isPerfect = Random.nextInt(100) < accuracy

                        // Flow 4.5: Calculate New Position
                        // If Perfect: Move ball towards Player 1 (-1)
                        // If Miss: Ball moves towards Bot (+1) - simulating a mistake
                        val move = if (isPerfect) -1 else 1
                        var newPos = currentPos + move

                        // Flow 4.6: Boundary Clamping
                        // Ensure ball stays within the -10 to 10 range
                        if (newPos > 10) newPos = 10
                        if (newPos < -10) newPos = -10

                        // Variable - Map - Prepare the update data
                        val updates = mutableMapOf<String, Any>("ballPosition" to newPos)

                        // Flow 4.7: Win/Loss Condition Check
                        // If ball hits 10 -> Player 1 Wins
                        if (newPos >= 10) {
                            updates["status"] = "finished"
                            updates["winnerId"] = snapshot.getString("player1Id") ?: ""
                        }
                        // If ball hits -10 -> Bot (Opponent) Wins
                        if (newPos <= -10) {
                            updates["status"] = "finished"
                            updates["winnerId"] = "opponent"
                        }

                        // Flow 4.8: Commit Updates
                        // Write the new position and status back to Firestore
                        transaction.update(db.collection("pvp_rooms").document(roomId), updates)
                    }
                }.addOnFailureListener {
                    // Flow 5.0: Error Handling
                    // Log error if transaction fails (e.g. network issue)
                }
            }
        }
    }
}