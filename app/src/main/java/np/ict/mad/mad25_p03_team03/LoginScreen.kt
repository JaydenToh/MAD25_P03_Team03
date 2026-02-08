package np.ict.mad.mad25_p03_team03

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

private val DarkBackground = Color(0xFF121212)
private val CardColor = Color(0xFF2F2F45)

@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBackground).padding(16.dp).background(DarkBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Song Guessing Game", style = MaterialTheme.typography.headlineLarge, color = Color.White)
        Spacer(Modifier.height(8.dp))
        Text("Test your music knowledge!", color = Color.White)

        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().border(0.dp, Color.White, RoundedCornerShape(16.dp))
                .shadow(6.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CardColor),
            shape = RoundedCornerShape(16.dp)

        ) {
            Column(Modifier.padding(16.dp)) {

                TextField(
                    value = username, onValueChange = { username = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                TextField(
                    value = password, onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (loginError.isNotEmpty()) {
                    Text(loginError, color = Color.Red)
                }

                Spacer(Modifier.height(16.dp))



                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            if (username.isNotEmpty()) {
                                auth.sendPasswordResetEmail(username)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                context,
                                                "Password reset email sent to $username. Please check your inbox.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            loginError = ""
                                        } else {
                                            loginError = task.exception?.localizedMessage ?: "Failed to send reset email."
                                        }
                                    }
                            } else {
                                loginError = "Please enter your email to reset the password."
                            }
                        }
                    ) {
                        Text("Forgot Password?", color = Color.White)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (username.isNotEmpty() && password.isNotEmpty()) {
                            auth.signInWithEmailAndPassword(username, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser

                                        if (user != null && user.isEmailVerified) {
                                            loginError = ""
                                            Toast.makeText(context, "Login Success", Toast.LENGTH_SHORT).show()
                                            onLoginSuccess()
                                        } else {
                                            user?.sendEmailVerification()
                                                ?.addOnSuccessListener {
                                                    Toast.makeText(context, "Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show()
                                                }
                                                ?.addOnFailureListener { e ->
                                                    Toast.makeText(context, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }

                                            loginError = "Please verify your email first!"
                                            auth.signOut() 
                                        }
                                    } else {
                                        loginError = task.exception?.message ?: "Invalid Username or password"
                                    }
                                }
                        } else {
                            loginError = "Please enter email and password"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Login", color = Color.White)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onSignUpClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Sign Up", color = Color.White)
                }
            }
        }
    }
}