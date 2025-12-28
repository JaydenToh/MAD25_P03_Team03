package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

data class ChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    friendId: String,
    friendName: String,
    onBack: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }

    // generate unique chat room ID based on user IDs
    val chatRoomId = getChatRoomId(currentUser?.uid ?: "", friendId)

    val listState = rememberLazyListState()

    // listen for new messages
    LaunchedEffect(chatRoomId) {
        if (currentUser != null) {
            db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    if (snapshot != null) {
                        messages = snapshot.toObjects(ChatMessage::class.java)
                    }
                }
        }
    }

    // auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(friendName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.senderId == currentUser?.uid
                    MessageBubble(message = message, isMe = isMe)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank() && currentUser != null) {
                            val newMessage = ChatMessage(
                                senderId = currentUser.uid,
                                text = messageText.trim(),
                                timestamp = System.currentTimeMillis()
                            )
                            db.collection("chats")
                                .document(chatRoomId)
                                .collection("messages")
                                .add(newMessage)


                            val chatRoomUpdate = mapOf(
                                "lastMessage" to messageText.trim(),
                                "lastSenderId" to currentUser.uid,
                                "lastTimestamp" to System.currentTimeMillis(),
                                "participants" to listOf(currentUser.uid, friendId)
                            )


                            db.collection("chats")
                                .document(chatRoomId)
                                .set(chatRoomUpdate, SetOptions.merge())

                            messageText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 0.dp,
                bottomEnd = if (isMe) 0.dp else 16.dp
            ),
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (isMe) Color.White else Color.Black,
                fontSize = 16.sp
            )
        }
    }
}

fun getChatRoomId(user1: String, user2: String): String {
    return if (user1 < user2) {
        "${user1}_${user2}"
    } else {
        "${user2}_${user1}"
    }
}