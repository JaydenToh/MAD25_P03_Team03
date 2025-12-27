package np.ict.mad.mad25_p03_team03.ui

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

data class Friend(
    val id: String,
    val username: String,
    val bio: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser

    var friends by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        currentUser?.uid?.let { uid ->
            // 1. 先获取当前用户的 friends 数组
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val friendIds = document.get("friends") as? List<String> ?: emptyList()

                    if (friendIds.isNotEmpty()) {
                        // 2. 如果有好友，用 whereIn 查询这些 ID 的用户详情
                        // 注意：whereIn 一次最多查 10 个，如果好友很多需要分批处理。
                        // 这里简单演示直接查询 (假设好友少于 10 个，或者手动循环查)

                        // 更稳妥的方法是手动循环 fetch，或者使用 whereIn (受限 10 个)
                        // 这里为了简单展示，我们循环获取 (对于小作业没问题)
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
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                            }
                        }
                    }
                }
            }
        }
    }
}