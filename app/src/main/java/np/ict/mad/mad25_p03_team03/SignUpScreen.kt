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
import com.google.firebase.firestore.FirebaseFirestore

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

                                            // 1. 准备要存入 Firestore 的数据
                                            // 注意：不要存 password！
                                            val userData = hashMapOf(
                                                "email" to email,
                                                "uid" to (user?.uid ?: ""),
                                                // 如果你有让用户输入名字，这里可以放名字，现在先用 email 前缀代替
                                                "username" to email.substringBefore("@"),
                                                "bio" to "New user",
                                                "createdAt" to com.google.firebase.Timestamp.now()
                                            )

                                            // 2. 获取 Firestore 实例
                                            val db = FirebaseFirestore.getInstance()

                                            // 3. 写入 users 集合，使用 uid 作为文档 ID
                                            user?.let {
                                                db.collection("users").document(it.uid)
                                                    .set(userData)
                                                    .addOnSuccessListener {
                                                        // 写入成功后再发送验证邮件或跳转
                                                        user.sendEmailVerification()
                                                        Toast.makeText(context, "Account Created & Profile Saved!", Toast.LENGTH_SHORT).show()
                                                        onBackToLoginClick()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        // 即使写入资料失败，账号其实已经注册了，这里可以做错误处理
                                                        Toast.makeText(context, "Account created but failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
                                                        onBackToLoginClick()
                                                    }
                                            }
                                        } else {
                                            error = task.exception?.localizedMessage ?: "Error"
                                        }
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