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

@Composable
fun MusicRoot(songRepository: SongRepository, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var notification by remember { mutableStateOf<NotificationData?>(null) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            db.collection("chats")
                .whereArrayContains("participants", currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == DocumentChange.Type.MODIFIED) {
                            val data = change.document.data
                            val lastSenderId = data["lastSenderId"] as? String
                            val lastMessage = data["lastMessage"] as? String ?: "New Message"

                            if (lastSenderId != null && lastSenderId != currentUser.uid) {
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

    LaunchedEffect(notification) {
        if (notification != null) {
            delay(3000)
            notification = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        MusicHome(navController = navController,songRepository = songRepository,onSignOut = onSignOut)

        AnimatedVisibility(
            visible = notification != null,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
                .zIndex(99f)
        ) {
            notification?.let { notif ->
                NotificationBanner(
                    data = notif,
                    onClick = {

                        val ids = notif.chatRoomId.split("_")
                        val friendId = ids.find { it != currentUser?.uid }

                        if (friendId != null) {
                            navController.navigate("chat/$friendId/Chat")
                        } else {
                            navController.navigate("friend_list")
                        }

                        notification = null
                    },
                    onDismiss = { notification = null }
                )
            }
        }
    }
}