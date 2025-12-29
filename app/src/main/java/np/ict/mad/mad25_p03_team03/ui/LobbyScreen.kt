package np.ict.mad.mad25_p03_team03.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
    val player1Id: String,
    val status: String,
    val mode: String,
    val gameType: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    songRepository: SongRepository,
    onNavigateToCreate: () -> Unit,
    onNavigateToGame: (String,String) -> Unit,
    onBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rooms by remember { mutableStateOf<List<GameRoom>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.collection("pvp_rooms")
            .whereEqualTo("status", "waiting")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    rooms = snapshot.documents.map { doc ->
                        GameRoom(
                            roomId = doc.id,
                            player1Name = doc.getString("player1Name") ?: "Unknown Player",
                            player1Id = doc.getString("player1Id") ?: "",
                            status = doc.getString("status") ?: "waiting",
                            mode = doc.getString("gameMode") ?: "ENGLISH",
                            gameType = doc.getString("gameType") ?: "TRIVIA"
                        )
                    }
                }
            }
    }

    fun createRoom() {
        if (currentUser == null) return
        isLoading = true

        scope.launch {
            val username = currentUser.email?.substringBefore("@") ?: "Player 1"

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
                "player1Name" to username,
                "player2Id" to null,
                "status" to "waiting",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "currentQuestionIndex" to 0,
                "scores" to hashMapOf(currentUser.uid to 0),
                "questions" to mappedQuestions
            )

            db.collection("pvp_rooms").add(newRoom)
                .addOnSuccessListener { docRef ->
                    isLoading = false
                    onNavigateToGame(docRef.id,"RHYTHM")
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Failed to create room", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun deleteRoom(roomId: String) {
        db.collection("pvp_rooms").document(roomId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Room deleted", Toast.LENGTH_SHORT).show()
            }
    }


    fun joinRoom(room: GameRoom) {
        if (currentUser == null) return
        if (room.status != "waiting") {
            Toast.makeText(context, "Room is no longer available", Toast.LENGTH_SHORT).show()
            return
        }


        db.collection("pvp_rooms").document(room.roomId)
            .update(
                mapOf(
                    "player2Id" to currentUser.uid,
                    "status" to "playing"
                )
            )
            .addOnSuccessListener {
                onNavigateToGame(room.roomId,room.gameType)
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
                onClick = { onNavigateToCreate() },
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
                        RoomItem(room = room,currentUserId = currentUser?.uid ?: "", onJoin = { joinRoom(room) },onDelete = { deleteRoom(room.roomId) })
                    }
                }
            }
        }
    }
}

@Composable
fun RoomItem(room: GameRoom,currentUserId: String, onJoin: () -> Unit,onDelete: () -> Unit) {
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
                    Text("Language: ${room.mode}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }



            if (room.player1Id == currentUserId) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = Color.Red)
                }
            } else {
                Button(onClick = onJoin) {
                    Text("Join")
                }
            }
        }
    }
}