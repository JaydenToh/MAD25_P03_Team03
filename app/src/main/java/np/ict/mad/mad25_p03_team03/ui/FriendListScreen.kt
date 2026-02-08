package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Variable - Color Theme - Custom colors for the dark theme UI
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White
private val GrayText = Color.Gray

// Class - Data Model - Represents a friend user
data class Friend(
    val id: String,
    val username: String,
    val bio: String
)

// Function - Main Screen - Displays list of added friends
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(
    onBack: () -> Unit,                          // Variable - Input - Navigation callback
    onChatClick: (String, String) -> Unit        // Variable - Input - Open chat with specific friend
) {
    // Flow 1.1: Dependency Setup
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    // Variable - State - UI Data
    var friends by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Flow 2.0: Data Fetching
    // Fetch the current user's friend list IDs and then details for each friend
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    // Logic - Get List of IDs
                    val friendIds = document.get("friends") as? List<String> ?: emptyList()

                    if (friendIds.isNotEmpty()) {
                        val fetchedFriends = mutableListOf<Friend>()
                        var completedCount = 0

                        // Logic - Fetch details for each ID
                        friendIds.forEach { friendId ->
                            db.collection("users").document(friendId).get()
                                .addOnSuccessListener { friendDoc ->
                                    if (friendDoc.exists()) {
                                        fetchedFriends.add(
                                            Friend(
                                                id = friendDoc.id,
                                                username = friendDoc.getString("username") ?: "Unknown",
                                                bio = friendDoc.getString("bio") ?: ""
                                            )
                                        )
                                    }
                                    completedCount++
                                    // Logic - Update State when all Done
                                    if (completedCount == friendIds.size) {
                                        friends = fetchedFriends
                                        isLoading = false
                                    }
                                }
                        }
                    } else {
                        isLoading = false
                    }
                }
                .addOnFailureListener { isLoading = false }
        }
    }

    // Flow 3.0: UI Construction
    Scaffold(
        containerColor = DarkBackground1, // Variable - Color - Dark Background
        topBar = {
            TopAppBar(
                title = { Text("My Friends", color = TextWhite) },
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
        // Flow 3.1: State Handling
        if (isLoading) {
            Box(Modifier.fillMaxSize().background(DarkBackground1), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurpleAccent)
            }
        } else if (friends.isEmpty()) {
            Box(Modifier.fillMaxSize().background(DarkBackground1), contentAlignment = Alignment.Center) {
                Text("No friends yet. Go to Leaderboard to add some!", color = GrayText)
            }
        } else {
            // Flow 3.2: List Rendering
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .background(DarkBackground1) // Ensure background is dark
            ) {
                items(friends) { friend ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onChatClick(friend.id, friend.username) },
                        colors = CardDefaults.cardColors(containerColor = CardColor1) // Variable - Color - Dark Card
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // UI - Avatar Placeholder
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = PurpleAccent
                            )
                            Spacer(Modifier.width(16.dp))

                            // UI - Friend Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(friend.username, style = MaterialTheme.typography.titleMedium, color = TextWhite)
                                if (friend.bio.isNotEmpty()) {
                                    Text(friend.bio, style = MaterialTheme.typography.bodySmall, color = GrayText)
                                }
                            }

                            // UI - Chat Button
                            IconButton(onClick = { onChatClick(friend.id, friend.username) }) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Message",
                                    tint = PurpleAccent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}