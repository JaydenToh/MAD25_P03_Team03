package np.ict.mad.mad25_p03_team03.ui

import android.widget.Toast
import androidx.compose.foundation.background
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

// Variable - Color Theme - Custom colors for the dark theme UI
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)
private val PurpleAccent = Color(0xFFBB86FC)
private val TextWhite = Color.White

// Class - Data Model - Represents a single game room in the list
data class GameRoom(
    val roomId: String,
    val player1Name: String,
    val player1Id: String,
    val status: String,
    val mode: String,
    val gameType: String
)

// Function - Main Screen - Displays the list of available multiplayer rooms
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    songRepository: SongRepository, // Variable - Repository - Access song data
    onNavigateToCreate: () -> Unit, // Variable - Navigation - Callback to create room screen
    onNavigateToGame: (String,String) -> Unit, // Variable - Navigation - Callback to game screen
    onBack: () -> Unit // Variable - Navigation - Callback to go back
) {
    // Flow 1.1: Dependency Injection & State Setup
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Variable - State - Holds the list of rooms fetched from Firestore
    var rooms by remember { mutableStateOf<List<GameRoom>>(emptyList()) }
    // Variable - State - Shows loading indicator if needed
    var isLoading by remember { mutableStateOf(false) }

    // Flow 2.0: Real-time Data Sync (Side Effect)
    LaunchedEffect(Unit) {
        // Flow 2.1: Query Firestore
        db.collection("pvp_rooms")
            .whereEqualTo("status", "waiting")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                // Flow 2.2: Handle Updates
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

    // Function - Internal Logic - Create room (Note: Currently unused in UI as FAB navigates away)
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

    // Function - Internal Logic - Deletes a room (Only the host can do this)
    // Flow 3.0: Delete Room Logic
    fun deleteRoom(roomId: String) {
        db.collection("pvp_rooms").document(roomId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Room deleted", Toast.LENGTH_SHORT).show()
            }
    }

    // Function - Internal Logic - Joins an existing room
    // Flow 4.0: Join Room Logic
    fun joinRoom(room: GameRoom) {
        // Flow 4.1: Validation
        if (currentUser == null) return
        if (room.status != "waiting") {
            Toast.makeText(context, "Room is no longer available", Toast.LENGTH_SHORT).show()
            return
        }

        // Flow 4.2: Atomic Update
        db.collection("pvp_rooms").document(room.roomId)
            .update(
                mapOf(
                    "player2Id" to currentUser.uid,
                    "status" to "playing"
                )
            )
            .addOnSuccessListener {
                // Flow 4.3: Navigation
                onNavigateToGame(room.roomId,room.gameType)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to join room", Toast.LENGTH_SHORT).show()
            }
    }

    // Flow 5.0: UI Layout Construction
    Scaffold(
        containerColor = DarkBackground1, // Variable - Color - Apply dark background
        topBar = {
            // Flow 5.1: Top Bar
            TopAppBar(
                title = { Text("Game Lobby", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground1
                )
            )
        },
        floatingActionButton = {
            // Flow 5.2: Create Room Button (FAB)
            ExtendedFloatingActionButton(
                onClick = { onNavigateToCreate() },
                icon = { Icon(Icons.Default.Add, "Create", tint = TextWhite) },
                text = { Text("Create Room", color = TextWhite) },
                containerColor = PurpleAccent // Variable - Color - Purple accent
            )
        }
    ) { padding ->
        // Flow 5.3: Main Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(DarkBackground1)
        ) {
            Text(
                "Available Rooms",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Flow 5.4: State-Based UI
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = PurpleAccent
                )
            }

            if (rooms.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No rooms found. Create one!", color = Color.Gray)
                }
            } else {
                // Flow 5.5: Render Room List
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(rooms) { room ->
                        RoomItem(
                            room = room,
                            currentUserId = currentUser?.uid ?: "",
                            onJoin = { joinRoom(room) },     // Trigger Flow 4.0
                            onDelete = { deleteRoom(room.roomId) } // Trigger Flow 3.0
                        )
                    }
                }
            }
        }
    }
}

// Function - UI Component - Renders a single card for a game room
// Flow 6.0: Room Card Component
@Composable
fun RoomItem(room: GameRoom,currentUserId: String, onJoin: () -> Unit,onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor1) // Variable - Color - Dark Blue/Grey
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Flow 6.1: Room Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MeetingRoom,
                    null,
                    tint = PurpleAccent // Variable - Color - Purple Tint
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        room.player1Name + "'s Room",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        "Language: ${room.mode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }


            // Flow 6.2: Action Buttons
            if (room.player1Id == currentUserId) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Room", tint = Color.Red)
                }
            } else {
                Button(
                    onClick = onJoin,
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                ) {
                    Text("Join", color = TextWhite)
                }
            }
        }
    }
}