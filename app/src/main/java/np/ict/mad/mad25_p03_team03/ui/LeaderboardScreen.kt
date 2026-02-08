package np.ict.mad.mad25_p03_team03.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

data class LeaderboardEntry(
    val userId: String,
    val username: String,
    val highScore: Int
)

@Composable
fun LeaderboardScreen(onPlayerClick: (String) -> Unit = {}) {
    var leaderboardData by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var myFriendsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()


        db.collection("users")
            .orderBy("highScore", Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener { result ->
                val entries = result.documents.mapNotNull { doc ->
                    val score = doc.getLong("highScore")?.toInt()

                    if (score != null) {
                        LeaderboardEntry(
                            userId = doc.id,
                            username = doc.getString("username") ?: "Unknown",
                            highScore = score
                        )
                    } else null
                }
                leaderboardData = entries
                isLoading = false
            }
            .addOnFailureListener { e ->
                errorMessage = "Failed to load leaderboard: ${e.message}"
                isLoading = false
            }
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        myFriendsList = snapshot.get("friends") as? List<String> ?: emptyList()
                    }
                }
        }
    }

    fun addFriend(targetUserId: String) {
        if (currentUser == null) return
        if (currentUser.uid == targetUserId) return

        val batch = db.batch()


        val myRef = db.collection("users").document(currentUser.uid)
        val targetRef = db.collection("users").document(targetUserId)

        batch.update(myRef, "friends", FieldValue.arrayUnion(targetUserId))

        batch.update(targetRef, "friends", FieldValue.arrayUnion(currentUser.uid))

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(context, "You are now friends!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add friend", Toast.LENGTH_SHORT).show()
            }
    }
    Scaffold(
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFFFFD700), // Gold color
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Leaderboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Text(
                text = "Top Players",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Yellow)
                }
            } else if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }
            } else if (leaderboardData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No scores yet. Be the first to play!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rank", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(40.dp))
                    Text("Player", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                    Text("Score", fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.width(48.dp))
                }


                // List
                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = paddingValues.calculateBottomPadding() + 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(leaderboardData) { index, entry ->
                        val isFriend = myFriendsList.contains(entry.userId)
                        val isMe = entry.userId == currentUser?.uid

                        LeaderboardItem(
                            rank = index + 1,
                            entry = entry,
                            isMe = isMe,
                            isFriend = isFriend,
                            onClick = { onPlayerClick(entry.userId) },
                            onAddFriend = { addFriend(entry.userId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardItem(rank: Int, entry: LeaderboardEntry,isMe: Boolean,
                    isFriend: Boolean,onClick: () -> Unit, onAddFriend: () -> Unit) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2F2F45)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    color = if (rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Username
            Text(
                text = entry.username,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                color = Color.White
            )

            // Score
            Text(
                text = "${entry.highScore}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Yellow
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Add Friend Button
            if (!isMe) {
                if (isFriend) {
                    Icon(
                        imageVector = Icons.Default.Favorite, 
                        contentDescription = "Is Friend",
                        tint = Color.Red.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    IconButton(onClick = onAddFriend) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Add Friend",
                            tint = if (isFriend) Color.Red else Color.White
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }
    }
}