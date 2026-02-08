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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

// Variable - Color Theme - Custom colors for the dark theme UI
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White
private val GrayText = Color.Gray

// Class - Data Model - Structure of a single chat message
data class ChatMessage(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

// Function - Main Screen - Real-time Chat Interface
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    friendId: String,       // Variable - Input - The ID of the friend you are chatting with
    friendName: String,     // Variable - Input - The name to display in the header
    onBack: () -> Unit      // Variable - Input - Navigation callback
) {
    // Flow 1.1: Dependency Setup
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // Variable - State - UI Input and Data
    var messageText by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }

    // Logic - Chat Room ID Generation
    // Ensures a unique ID regardless of who started the chat (e.g., "userA_userB")
    val chatRoomId = getChatRoomId(currentUser?.uid ?: "", friendId)

    val listState = rememberLazyListState()

    // Flow 2.0: Real-time Data Sync
    // Listens for new messages in the specific chat room
    LaunchedEffect(chatRoomId) {
        if (currentUser != null) {
            db.collection("chats")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    // Logic - Handle updates
                    if (e != null) return@addSnapshotListener
                    if (snapshot != null) {
                        messages = snapshot.toObjects(ChatMessage::class.java)
                    }
                }
        }
    }

    // Flow 3.0: Auto-Scroll Logic
    // Automatically scrolls to the newest message when the list updates
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Flow 4.0: UI Construction
    Scaffold(
        containerColor = DarkBackground1, // Variable - Color - Dark Background
        topBar = {
            TopAppBar(
                title = { Text(friendName, color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground1
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(DarkBackground1) // Ensure background is dark
        ) {
            // Flow 4.1: Message List
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

            // Flow 4.2: Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Type a message...", color = GrayText) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = PurpleAccent,
                        focusedBorderColor = PurpleAccent,
                        unfocusedBorderColor = GrayText
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))

                // UI - Send Button
                IconButton(
                    onClick = {
                        // Flow 5.0: Send Message Logic
                        if (messageText.isNotBlank() && currentUser != null) {
                            val newMessage = ChatMessage(
                                senderId = currentUser.uid,
                                text = messageText.trim(),
                                timestamp = System.currentTimeMillis()
                            )

                            // Logic - Add message to sub-collection
                            db.collection("chats")
                                .document(chatRoomId)
                                .collection("messages")
                                .add(newMessage)

                            // Logic - Update room metadata (for recent chats list)
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
                        .background(PurpleAccent, androidx.compose.foundation.shape.CircleShape) // Variable - Color - Accent
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = TextWhite)
                }
            }
        }
    }
}

// Function - UI Component - Single Message Bubble
// Flow 6.0: Message Bubble Component
@Composable
fun MessageBubble(message: ChatMessage, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            // Logic - Conditional Styling based on sender
            color = if (isMe) PurpleAccent else CardColor1,
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
                color = TextWhite, // Always white text for contrast
                fontSize = 16.sp
            )
        }
    }
}

// Function - Utility - Generates consistent Room ID
fun getChatRoomId(user1: String, user2: String): String {
    return if (user1 < user2) {
        "${user1}_${user2}"
    } else {
        "${user2}_${user1}"
    }
}