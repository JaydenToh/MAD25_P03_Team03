package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import androidx.compose.material.icons.filled.Chat

data class Friend(
    val id: String,
    val username: String,
    val bio: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(onBack: () -> Unit,onChatClick: (String, String) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var friends by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val friendIds = document.get("friends") as? List<String> ?: emptyList()

                    if (friendIds.isNotEmpty()) {
                        val fetchedFriends = mutableListOf<Friend>()
                        var completedCount = 0

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Friends") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (friends.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No friends yet. Go to Leaderboard to add some!")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                items(friends) { friend ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onChatClick(friend.id, friend.username) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(friend.username, style = MaterialTheme.typography.titleMedium)
                                if (friend.bio.isNotEmpty()) {
                                    Text(friend.bio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onChatClick(friend.id, friend.username) }) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "Message",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}