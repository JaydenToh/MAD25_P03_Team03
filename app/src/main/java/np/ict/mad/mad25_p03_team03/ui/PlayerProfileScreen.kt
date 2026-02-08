package np.ict.mad.mad25_p03_team03.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

// Function - UI Screen - Displays a specific user's public profile
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerProfileScreen(
    userId: String,     // Variable - Input - The unique ID of the user to fetch
    onBack: () -> Unit  // Variable - Input - Callback to handle back navigation
) {
    // Variable - Service - Firestore instance for database operations
    // Flow 1.1: Dependency Setup
    val db = FirebaseFirestore.getInstance()
    // Variable - Context - Current context for Toasts
    val context = LocalContext.current

    // Variable - State - UI State holders for profile data
    // Flow 1.2: State Initialization
    var username by remember { mutableStateOf("Loading...") }
    var bio by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) }
    var highScore by remember { mutableStateOf(0) }

    // Logic - Side Effect - Fetch specific player data from Firestore
    // Flow 2.0: Data Fetching
    LaunchedEffect(userId) {
        // Flow 2.1: Database Query
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                // Flow 2.2: Response Handling
                if (document.exists()) {
                    // Flow 2.3: Data Mapping
                    username = document.getString("username") ?: "Unknown User"
                    bio = document.getString("bio") ?: "No bio provided."
                    highScore = document.getLong("highScore")?.toInt() ?: 0
                } else {
                    // Flow 2.4: Empty State
                    username = "User Not Found"
                }
                // Flow 2.5: Stop Loading
                isLoading = false
            }
            .addOnFailureListener {
                // Flow 2.6: Error Handling
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    // UI - Layout - Main Container
    // Flow 3.0: UI Construction
    Scaffold(
        containerColor = Color(0xFF121212), // Variable - Color - Dark Background
        topBar = {
            // Flow 3.1: Top App Bar
            TopAppBar(
                title = { Text("Player Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) { // Flow 3.2: Back Action
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212), // Flow 3.3: Bar Color
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // Flow 4.0: Content Switching
        if (isLoading) {
            // Flow 4.1: Loading View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Flow 4.2: Profile Content View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // UI - Component - Avatar Circle
                // Flow 5.0: Avatar Section
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color(0xFF2F2F45) // Variable - Color - Surface Dark
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // UI - Component - Username Text
                // Flow 6.0: Username Display
                Text(
                    text = username,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                // UI - Component - High Score Card
                // Flow 7.0: Score Display
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2F2F45) // Flow 7.1: Card Background
                    )
                ) {
                    Text(
                        text = "High Score: $highScore",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Yellow // Flow 7.2: Highlight Color
                    )
                }

                Spacer(Modifier.height(32.dp))

                // UI - Component - Bio Section
                // Flow 8.0: Bio Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2F2F45)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Flow 8.1: Bio Header
                        Text(
                            text = "About Me",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        Spacer(Modifier.height(8.dp))
                        // Flow 8.2: Bio Content
                        Text(
                            text = bio,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}