package np.ict.mad.mad25_p03_team03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    App()
                }
            }
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object SignUp : Screen()
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }

    when (currentScreen) {
        is Screen.Login -> LoginScreen(
            onSignUpClick = { currentScreen = Screen.SignUp }
        )
        is Screen.SignUp -> SignUpScreen(
            onBackToLoginClick = { currentScreen = Screen.Login }
        )
    }
}

@Composable
fun LoginScreen(
    onSignUpClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF59168B), // Purple
            Color(0xFF1C398E), // Blue
            Color(0xFF312C85)  // Dark Purple
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Song Guessing Game",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.White
        )

        Text(
            text = "Test your music knowledge!",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp),
            color = Color.White
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {

                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                if (loginError.isNotEmpty()) {
                    Text(
                        text = loginError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = {
                        if (username == "admin" && password == "admin123") {
                            loginError = ""
                        } else {
                            loginError = "Username or Password Incorrect"
                        }
                    },
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text(text = "Login")
                }

                Button(
                    onClick = onSignUpClick,
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text(text = "Sign Up")
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    onBackToLoginClick: () -> Unit = {}
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var signUpError by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val existingUsers = listOf("admin", "testuser")

    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF59168B), // Purple
            Color(0xFF1C398E), // Blue
            Color(0xFF312C85)  // Dark Purple
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Song Guessing Game",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.White
        )

        Text(
            text = "Test your music knowledge!",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 32.dp),
            color = Color.White
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                .shadow(4.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {

                TextField(
                    value = username,
                    onValueChange = {
                        username = it
                        successMessage = ""
                    },
                    label = { Text("Username") },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        successMessage = ""
                    },
                    label = { Text("Password") },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                TextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        successMessage = ""
                    },
                    label = { Text("Confirm Password") },
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )

                if (signUpError.isNotEmpty()) {
                    Text(
                        text = signUpError,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (successMessage.isNotEmpty()) {
                    Text(
                        text = successMessage,
                        color = Color(0xFF00DC00),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = {

                        when {
                            username.isBlank() || password.isBlank() || confirmPassword.isBlank() ->
                                signUpError = "Please fill in all fields"

                            username in existingUsers ->
                                signUpError = "Username already taken"

                            password != confirmPassword ->
                                signUpError = "Passwords do not match"

                            else -> {
                                signUpError = ""
                                successMessage = "Created Account Successfully!"
                            }
                        }

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(5.dp)
                )

                {
                    Text(text = "Sign Up")
                }

                Button(
                    onClick = onBackToLoginClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text(text = "Back to Login")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    MaterialTheme {
        SignUpScreen()
    }
}
