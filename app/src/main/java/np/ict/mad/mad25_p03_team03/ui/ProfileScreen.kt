package np.ict.mad.mad25_p03_team03.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun ProfileScreen() {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // 定义状态来保存从 Firestore 拿到的数据
    var username by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("Loading...") }

    // 当页面加载时，去 Firestore 抓取数据
    LaunchedEffect(key1 = currentUser) {
        if (currentUser != null) {
            // 1. 优先显示 Auth 里的 Email（这是最准确的）
            email = currentUser.email ?: "No Email"

            // 2. 根据 UID 去 Firestore 找详细资料
            db.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // 读取字段
                        username = document.getString("username") ?: "No Name"
                        bio = document.getString("bio") ?: "No Bio available"
                    } else {
                        username = "User"
                        bio = "Profile not set up."
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // 头像部分
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // 显示名字
        Text(
            text = username,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        // 显示 Email (带图标)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }

        // 显示是否已验证邮箱
        if (currentUser?.isEmailVerified == true) {
            Text("✅ Verified Account", color = Color(0xFF00AA00), style = MaterialTheme.typography.bodySmall)
        } else {
            Text("⚠️ Email not verified", color = Color.Red, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(32.dp))

        // 信息卡片 (Bio)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Bio",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = bio,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 如果你想让用户改密码，可以在这里加按钮，而不是显示密码
        Button(
            onClick = {
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Reset password email sent!", Toast.LENGTH_SHORT).show()
                    }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Change Password")
        }
    }
}