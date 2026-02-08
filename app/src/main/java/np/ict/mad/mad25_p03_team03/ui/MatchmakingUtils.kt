// Place in ModeSelectionScreen.kt or a separate file like MatchmakingUtils.kt
package np.ict.mad.mad25_p03_team03.ui

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import np.ict.mad.mad25_p03_team03.data.SongRepository

// Function - Helper Logic - Handles matchmaking: finds an open room or creates a new one
// Flow 1.0: Entry Point
fun findOrCreateGame(
    db: FirebaseFirestore,          // Variable - Input - Firestore Database Instance
    currentUser: FirebaseUser,      // Variable - Input - The currently logged-in user
    songRepository: SongRepository, // Variable - Input - Repository to fetch questions (passed for context)
    onGameFound: (String) -> Unit,  // Variable - Input - Callback triggered when a room ID is ready
    onFail: (String) -> Unit        // Variable - Input - Callback triggered on error
) {
    // Flow 1.1: Query Firestore
    // Look for any document in 'pvp_rooms' where status is 'waiting'
    db.collection("pvp_rooms")
        .whereEqualTo("status", "waiting")
        .limit(1) // Logic - Optimization: We only need one available slot
        .get()
        .addOnSuccessListener { snapshot ->
            // Flow 2.0: Check Results
            if (!snapshot.isEmpty) {
                // Flow 2.1: Room Found Logic (Join Path)
                // Get the first available room document
                val room = snapshot.documents[0]
                val roomId = room.id

                // Flow 2.2: Self-Join Prevention
                // Guard clause: Ensure the player doesn't join a room they created themselves (stale room)
                if (room.getString("player1Id") == currentUser.uid) {
                    onGameFound(roomId)
                    return@addOnSuccessListener
                }

                // Flow 2.3: Join Operation
                // Update the room document to register Player 2 and start the game immediately
                db.collection("pvp_rooms").document(roomId)
                    .update(
                        mapOf(
                            "player2Id" to currentUser.uid,
                            "status" to "playing" // Logic - State Change to 'playing'
                        )
                    )
                    .addOnSuccessListener {
                        // Flow 2.4: Success Callback
                        onGameFound(roomId)
                    }
                    .addOnFailureListener {
                        // Flow 2.5: Failure Callback
                        onFail("Failed to join room")
                    }

            } else {
                // Flow 3.0: Create Room Logic (Create Path)
                // No waiting rooms found, so we must create a new one
                // Logic - Note: Question generation is deferred to the Game Screen to keep this UI responsive
                createRoomWithQuestions(db, currentUser, songRepository, onGameFound, onFail)
            }
        }
        .addOnFailureListener {
            // Flow 1.3: Query Failure
            onFail(it.message ?: "Error finding room")
        }
}

// Function - Helper Logic - Generates a new room document in Firestore
// Flow 4.0: Creation Entry Point
private fun createRoomWithQuestions(
    db: FirebaseFirestore,          // Variable - Input - DB Instance
    currentUser: FirebaseUser,      // Variable - Input - Host User
    songRepository: SongRepository, // Variable - Input - Data Source
    onSuccess: (String) -> Unit,    // Variable - Input - Success Callback
    onFail: (String) -> Unit        // Variable - Input - Failure Callback
) {
    // Flow 4.1: Data Construction
    // Create the initial state map for the new game room
    val newRoom = hashMapOf(
        "player1Id" to currentUser.uid,
        "player2Id" to null,
        "status" to "waiting",
        "currentQuestionIndex" to 0,
        "scores" to hashMapOf(currentUser.uid to 0),
        "roundWinnerId" to null,
        // Logic - Initialize empty questions list
        // Note: Actual song data will be populated by Player 1 in the Game Screen to handle async loading better
        "questions" to emptyList<Map<String, Any>>()
    )

    // Flow 4.2: Database Write
    // Add the new map to the 'pvp_rooms' collection
    db.collection("pvp_rooms").add(newRoom)
        .addOnSuccessListener { docRef ->
            // Flow 4.3: Creation Success
            onSuccess(docRef.id)
        }
        .addOnFailureListener {
            // Flow 4.4: Creation Failure
            onFail(it.message ?: "Failed to create")
        }
}