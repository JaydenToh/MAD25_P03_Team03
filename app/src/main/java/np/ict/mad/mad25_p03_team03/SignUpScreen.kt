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

@Composable
fun SignUpScreen(onBackToLoginClick: () -> Unit) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    val gradientBrush = Brush.linearGradient(
        listOf(Color(0xFF59168B), Color(0xFF1C398E), Color(0xFF312C85))
    )

    Column(
        modifier = Modifier.fillMaxSize().background(gradientBrush).padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineLarge, color = Color.White)

        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth().border(2.dp, Color.White, RoundedCornerShape(16.dp))
                .shadow(6.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {

                TextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))

                TextField(password, { password = it }, label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))

                TextField(confirm, { confirm = it }, label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

                if (error.isNotEmpty()) Text(error, color = Color.Red)

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        when {
                            email.isBlank() || password.isBlank() || confirm.isBlank() ->
                                error = "All fields required"

                            password != confirm ->
                                error = "Passwords do not match"

                            else -> {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = auth.currentUser

                                            // [新增] 注册成功后立刻发送验证邮件
                                            user?.sendEmailVerification()
                                                ?.addOnCompleteListener { verifyTask ->
                                                    if (verifyTask.isSuccessful) {
                                                        Toast.makeText(context, "Sign up success! Verification email sent. Please check your inbox.", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        // 可选: 如果发送失败，也给用户一个提示
                                                        Toast.makeText(context, "Sign up success, but failed to send verification email.", Toast.LENGTH_LONG).show()
                                                    }
                                                }

                                            // 确保用户看到 Toast 提示后，再执行跳转
                                            // 注意：由于 Firebase 的回调是异步的，Toast 可能会在跳转之后才显示。
                                            // 在 Compose 中，直接调用 onBackToLoginClick() 通常是可行的。
                                            onBackToLoginClick()
                                        } else error = task.exception?.localizedMessage ?: "Error"
                                    }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Sign Up", color = Color.White)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onBackToLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Back to Login", color = Color.White)
                }
            }
        }
    }
}