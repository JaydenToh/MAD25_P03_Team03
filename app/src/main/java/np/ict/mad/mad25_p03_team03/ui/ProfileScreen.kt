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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onViewFriends: () -> Unit = {}) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var username by remember { mutableStateOf("Loading...") }
    val email by remember { mutableStateOf(currentUser?.email ?: "N/A") }
    var bio by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(false) }
    var friendsCount by remember { mutableStateOf(0) }

    val fetchData: () -> Unit = {
        isLoading = true
        currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username") ?: "Set Username"
                        bio = document.getString("bio") ?: "Set your bio here"

                        val friends = document.get("friends") as? List<String> ?: emptyList()
                        friendsCount = friends.size
                    } else {
                        username = currentUser.email?.substringBefore("@") ?: "User"
                        bio = "Welcome! Set your bio."
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
        } ?: run {
            isLoading = false
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    val saveProfile: () -> Unit = {
        if (currentUser?.uid == null) {
            Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_SHORT).show()
        } else if (username.isBlank()) {
            Toast.makeText(context, "Username cannot be empty.", Toast.LENGTH_SHORT).show()
        } else {
            isLoading = true
            val updatedData = mapOf(
                "username" to username,
                "bio" to bio
            )

            db.collection("users").document(currentUser.uid)
                .update(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
                    isLoading = false
                }
        }
    }


    LaunchedEffect(key1 = currentUser) {
        fetchData()
    }

    Scaffold(
        containerColor = Color(0xFF121212),
        topBar = {
            TopAppBar(
                title = { Text("My Profile", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212)
                ),
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFFBB86FC),
                            modifier = Modifier.size(24.dp).padding(end = 16.dp)
                        )
                    } else {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)

                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))


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


            val isVerified = currentUser?.isEmailVerified == true
            Text(
                if (isVerified) "✅ Verified Account" else "⚠️ Email not verified (Check Login Page)",
                color = if (isVerified) Color(0xFF00AA00) else Color(0xFFFF5555),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start).padding(start = 16.dp)
            )

            Spacer(Modifier.height(24.dp))


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