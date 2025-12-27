package np.ict.mad.mad25_p03_team03.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
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
import np.ict.mad.mad25_p03_team03.data.GameMode
import np.ict.mad.mad25_p03_team03.data.SongRepository
import kotlinx.coroutines.launch

data class GameRoom(
    val roomId: String,
    val player1Name: String,
    val status: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    songRepository: SongRepository,
    onNavigateToGame: (String) -> Unit, // 传入 roomId
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rooms by remember { mutableStateOf<List<GameRoom>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    // 1. 实时监听：获取所有状态为 "waiting" 的房间
    LaunchedEffect(Unit) {
        db.collection("pvp_rooms")
            .whereEqualTo("status", "waiting")
            .orderBy("createdAt", Query.Direction.DESCENDING) // 记得在 Firestore 建索引
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    rooms = snapshot.documents.map { doc ->
                        GameRoom(
                            roomId = doc.id,
                            player1Name = doc.getString("player1Name") ?: "Unknown Player",
                            status = doc.getString("status") ?: "waiting"
                        )
                    }
                }
            }
    }

    // 2. 创建房间逻辑
    fun createRoom() {
        if (currentUser == null) return
        isLoading = true

        scope.launch {
            // 获取当前玩家的名字 (可选: 如果你之前存过 username 可以先 fetch 一下，这里为了简单直接用 Email 或 ID)
            val username = currentUser.email?.substringBefore("@") ?: "Player 1"

            // 预先生成题目
            val songs = songRepository.fetchSongsFromSupabase(GameMode.ENGLISH).take(5)
            val mappedQuestions = songs.map { song ->
                mapOf(
                    "correctTitle" to song.title,
                    "options" to (listOf(song.title) + song.fakeOptions).shuffled().take(4),
                    "audioUrl" to song.audioUrl
                )
            }

            val newRoom = hashMapOf(
                "player1Id" to currentUser.uid,
                "player1Name" to username, // 存名字方便列表显示
                "player2Id" to null,
                "status" to "waiting",
                "createdAt" to com.google.firebase.Timestamp.now(), // 用于排序
                "currentQuestionIndex" to 0,
                "scores" to hashMapOf(currentUser.uid to 0),
                "questions" to mappedQuestions
            )

            db.collection("pvp_rooms").add(newRoom)
                .addOnSuccessListener { docRef ->
                    isLoading = false
                    // 创建成功，直接进入房间 (PvpGameScreen 会处理 waiting UI)
                    onNavigateToGame(docRef.id)
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to create room", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // 3. 加入房间逻辑
    fun joinRoom(room: GameRoom) {
        if (currentUser == null) return
        if (room.status != "waiting") {
            Toast.makeText(context, "Room is no longer available", Toast.LENGTH_SHORT).show()
            return
        }

        // 简单防止自己进自己房间 (虽然 UI 上可以不用禁，但逻辑上最好防一下)
        // 这里的校验最好是在 PvpGameScreen 做，或者这里先 fetch 检查 player1Id
        // 为了流畅，我们直接尝试加入，PvpGameScreen 里的 Transaction 会保证安全性

        db.collection("pvp_rooms").document(room.roomId)
            .update(
                mapOf(
                    "player2Id" to currentUser.uid,
                    "status" to "playing"
                )
            )
            .addOnSuccessListener {
                onNavigateToGame(room.roomId)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to join room", Toast.LENGTH_SHORT).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Lobby") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { createRoom() },
                icon = { Icon(Icons.Default.Add, "Create") },
                text = { Text("Create Room") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Available Rooms",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (rooms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No rooms found. Create one!", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(rooms) { room ->
                        RoomItem(room = room, onJoin = { joinRoom(room) })
                    }
                }
            }
        }
    }
}

@Composable
fun RoomItem(room: GameRoom, onJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MeetingRoom, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(room.player1Name + "'s Room", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Status: Waiting", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Button(onClick = onJoin) {
                Text("Join")
            }
        }
    }
}