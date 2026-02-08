package np.ict.mad.mad25_p03_team03.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Function - Main Screen - User Profile Management
// Flow 1.0: Screen Entry Point
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onViewFriends: () -> Unit = {} // Variable - Input - Callback to navigate to friends list
) {
    // Flow 1.1: Dependency Setup
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Variable - State - User Profile Data
    // Flow 1.2: State Initialization
    var username by remember { mutableStateOf("Loading...") }
    val email by remember { mutableStateOf(currentUser?.email ?: "N/A") }
    var bio by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(false) }
    var friendsCount by remember { mutableStateOf(0) }

    // Function - Helper Logic - Fetches user data from Firestore
    // Flow 2.0: Data Fetching Logic
    val fetchData: () -> Unit = {
        isLoading = true
        // Flow 2.1: User ID Check
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    // Flow 2.2: Document Parsing
                    if (document.exists()) {
                        username = document.getString("username") ?: "Set Username"
                        bio = document.getString("bio") ?: "Set your bio here"

                        // Logic - Get Friend Count
                        val friends = document.get("friends") as? List<String> ?: emptyList()
                        friendsCount = friends.size
                    } else {
                        // Flow 2.3: New User Fallback
                        username = currentUser.email?.substringBefore("@") ?: "User"
                        bio = "Welcome! Set your bio."
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    // Flow 2.4: Error Handling
                    Toast.makeText(context, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        } ?: run {
            isLoading = false
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    // Function - Helper Logic - Saves updated profile to Firestore
    // Flow 3.0: Data Saving Logic
    val saveProfile: () -> Unit = {
        // Flow 3.1: Validation
        if (currentUser?.uid == null) {
            Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
        } else if (username.isBlank()) {
            Toast.makeText(context, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
        } else {
            isLoading = true
            // Variable - Map - Data payload
            val updatedData = mapOf(
                "username" to username,
                "bio" to bio
            )

            // Flow 3.2: Database Update
            db.collection("users").document(currentUser.uid)
                .update(updatedData)
                .addOnSuccessListener {
                    // Flow 3.3: Success Feedback
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    // Flow 3.4: Error Feedback
                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                    isLoading = false
                }
        }
    }

    // Flow 4.0: Initial Data Load
    LaunchedEffect(key1 = currentUser) {
        fetchData()
    }

    // Flow 5.0: UI Construction
    Scaffold(
        containerColor = Color(0xFF121212), // Variable - Color - Dark Background
        topBar = {
            // Flow 5.1: Top App Bar
            TopAppBar(
                title = { Text("My Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                ),
                actions = {
                    // Flow 5.2: Action Button Logic
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFFBB86FC),
                            modifier = Modifier.size(24.dp).padding(end = 16.dp)
                        )
                    } else {
                        // UI - Button - Save Changes
                        Button(
                            onClick = saveProfile,
                            enabled = currentUser != null && !isLoading,
                            modifier = Modifier.padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2F2F45),
                                contentColor = Color.White
                            ),
                        ) {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Flow 6.0: Scrollable Content Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // UI - Component - Avatar
            // Flow 6.1: Avatar Display
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color(0xFF2F2F45)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // UI - Component - Friends Link Card
            // Flow 6.2: Friends List Navigation
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        onViewFriends()
                    },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F2F45))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "My Friends: $friendsCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.weight(1f))
                    Text("View >", color = Color.Gray)
                }
            }

            // UI - Input - Username Field
            // Flow 6.3: Username Edit
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2F2F45),
                    unfocusedContainerColor = Color(0xFF2F2F45),
                    focusedBorderColor = Color(0xFFBB86FC),
                    unfocusedBorderColor = Color.Transparent,
                    focusedLabelColor = Color(0xFFBB86FC),
                    unfocusedLabelColor = Color(0xFFB0B0B0),
                    cursorColor = Color(0xFFBB86FC)
                )
            )

            // UI - Input - Email Field (Read Only)
            // Flow 6.4: Email Display
            OutlinedTextField(
                value = email,
                onValueChange = { /* Email read-only */ },
                label = { Text("Email (Cannot be changed here)") },
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFFB0B0B0),
                    unfocusedTextColor = Color(0xFFB0B0B0),
                    focusedContainerColor = Color(0xFF2F2F45),
                    unfocusedContainerColor = Color(0xFF2F2F45),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedLabelColor = Color(0xFFB0B0B0),
                    unfocusedLabelColor = Color(0xFFB0B0B0)
                )
            )

            // UI - Status - Verification Check
            // Flow 6.5: Email Verification Status
            val isVerified = currentUser?.isEmailVerified == true
            Text(
                if (isVerified) "✅ Verified Account" else "⚠️ Email not verified (Check Login Page)",
                color = if (isVerified) Color(0xFF00AA00) else Color(0xFFFF5555),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
            )

            Spacer(Modifier.height(24.dp))

            // UI - Input - Bio Field
            // Flow 6.6: Bio Edit
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio / About Me") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp).padding(bottom = 32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2F2F45),
                    unfocusedContainerColor = Color(0xFF2F2F45),
                    focusedBorderColor = Color(0xFFBB86FC),
                    unfocusedBorderColor = Color.Transparent,
                    focusedLabelColor = Color(0xFFBB86FC),
                    unfocusedLabelColor = Color(0xFFB0B0B0),
                    cursorColor = Color(0xFFBB86FC)
                )
            )

            // UI - Button - Password Reset
            // Flow 6.7: Password Reset Action
            Button(
                onClick = {
                    if (email.isNotEmpty() && email != "N/A") {
                        auth.sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Password reset email sent to $email!", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to send reset email. Make sure your email is correct.", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Cannot send reset link. Email is not available.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F2F45))
            ) {
                Text("Send Password Reset Link")
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}