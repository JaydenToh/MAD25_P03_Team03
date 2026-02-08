package np.ict.mad.mad25_p03_team03.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.ui.MusicHome
import np.ict.mad.mad25_p03_team03.ui.NotificationBanner
import np.ict.mad.mad25_p03_team03.ui.NotificationData

// Function - Main Composable - Root entry point for the Music App section
// Flow 1.0: Screen Entry Point
@Composable
fun MusicRoot(
    songRepository: SongRepository, // Variable - Dependency - Data source for songs
    onSignOut: () -> Unit           // Variable - Callback - Action to perform on sign out
) {
    // Variable - State - Manages navigation within the music section
    // Flow 1.1: Navigation Setup
    val navController = rememberNavController()

    // Variable - Service - Firestore instance for real-time updates
    // Flow 1.2: Database Setup
    val db = FirebaseFirestore.getInstance()

    // Variable - Auth - Current logged-in user
    // Flow 1.3: User Context
    val currentUser = FirebaseAuth.getInstance().currentUser

    // Variable - State - Holds the current notification data (if any)
    // Flow 1.4: Notification State
    var notification by remember { mutableStateOf<NotificationData?>(null) }

    // Logic - Side Effect - Listens for new chat messages in real-time
    // Flow 2.0: Listener Initialization
    LaunchedEffect(currentUser) {
        // Flow 2.1: User Check
        if (currentUser != null) {
            // Flow 2.2: Query Construction
            db.collection("chats")
                .whereArrayContains("participants", currentUser.uid)
                .addSnapshotListener { snapshot, e -> // Flow 2.3: Snapshot Listener
                    // Flow 2.4: Error Handling
                    if (e != null) return@addSnapshotListener

                    // Flow 2.5: Change Iteration
                    snapshot?.documentChanges?.forEach { change ->
                        // Flow 2.6: Modification Check
                        if (change.type == DocumentChange.Type.MODIFIED) {
                            // Flow 2.7: Data Extraction
                            val data = change.document.data
                            val lastSenderId = data["lastSenderId"] as? String
                            val lastMessage = data["lastMessage"] as? String ?: "New Message"

                            // Flow 2.8: Notification Logic
                            // Only notify if the message is NOT from the current user
                            if (lastSenderId != null && lastSenderId != currentUser.uid) {
                                // Flow 2.9: State Update
                                notification = NotificationData(
                                    senderName = "Chat Message",
                                    message = lastMessage,
                                    chatRoomId = change.document.id,
                                    senderId = lastSenderId
                                )
                            }
                        }
                    }
                }
        }
    }

    // Logic - Side Effect - Auto-dismiss notification after 3 seconds
    // Flow 3.0: Timer Logic
    LaunchedEffect(notification) {
        // Flow 3.1: Active Check
        if (notification != null) {
            // Flow 3.2: Delay Interval
            delay(3000)
            // Flow 3.3: Reset State
            notification = null
        }
    }

    // UI - Layout - Main Container
    // Flow 4.0: Root UI Structure
    Box(modifier = Modifier.fillMaxSize()) {
        // Flow 4.1: Content Rendering
        MusicHome(
            navController = navController,
            songRepository = songRepository,
            onSignOut = onSignOut
        )

        // UI - Animation - Notification Banner Slide In/Out
        // Flow 5.0: Notification Animation
        AnimatedVisibility(
            visible = notification != null,
            enter = slideInVertically(initialOffsetY = { -it }), // Flow 5.1: Enter Animation
            exit = slideOutVertically(targetOffsetY = { -it }),  // Flow 5.2: Exit Animation
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .zIndex(99f) // Flow 5.3: Z-Index Layering
        ) {
            // Flow 5.4: Banner Rendering
            notification?.let { notif ->
                NotificationBanner(
                    data = notif,
                    onClick = {
                        // Flow 6.0: Click Handling
                        // Logic to extract friend ID from room ID (e.g., "userA_userB")
                        val ids = notif.chatRoomId.split("_") // Flow 6.1: ID Parsing
                        val friendId = ids.find { it != currentUser?.uid } // Flow 6.2: Friend Identification

                        // Flow 6.3: Navigation Logic
                        if (friendId != null) {
                            navController.navigate("chat/$friendId/Chat")
                        } else {
                            navController.navigate("friend_list")
                        }

                        // Flow 6.4: Dismiss on Click
                        notification = null
                    },
                    onDismiss = {
                        // Flow 6.5: Manual Dismiss
                        notification = null
                    }
                )
            }
        }
    }
}