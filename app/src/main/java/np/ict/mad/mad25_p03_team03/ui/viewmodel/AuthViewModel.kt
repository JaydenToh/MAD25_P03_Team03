// File: ui/viewmodel/AuthViewModel.kt

package np.ict.mad.mad25_p03_team03.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // 临时用户存储（示例：使用内存缓存，实际应用应使用 SharedPreferences 或 Room）
    private val tempUsers = mutableMapOf<String, String>() // email -> password

    // UI 状态流
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    suspend fun sendEmailVerification(email: String): Result<Unit> {
        return try {
            // 生成临时密码
            val password = generateRandomPassword()
            Log.d("AuthViewModel", "Creating user with email: $email, password: $password")

            // 创建用户
            val userCredential = auth.createUserWithEmailAndPassword(email, password).await()

            // 发送验证邮件
            userCredential.user?.sendEmailVerification()?.await()

            // 保存临时密码（用于后续登录）
            saveTempUser(email, password)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to send verification email", e)
            Result.failure(e)
        }
    }

    suspend fun verifyEmailAndLogin(email: String): Result<FirebaseUser> {
        return try {
            // 获取临时密码
            val tempPassword = getTempPassword(email) ?: return Result.failure(Exception("No temporary password found"))

            // 登录
            val userCredential = auth.signInWithEmailAndPassword(email, tempPassword).await()

            // 检查邮箱是否已验证
            if (userCredential.user?.isEmailVerified == true) {
                // 登录成功 → 清除临时数据
                clearTempUser(email)
                Result.success(userCredential.user!!)
            } else {
                Result.failure(Exception("Email not verified"))
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Failed to verify and login", e)
            Result.failure(e)
        }
    }

    private fun generateRandomPassword(): String {
        return "TempPass" + Random.nextInt(1000, 9999)
    }

    private fun saveTempUser(email: String, password: String) {
        tempUsers[email] = password
        Log.d("AuthViewModel", "Saved temp user: $email")
    }

    private fun getTempPassword(email: String): String? {
        return tempUsers[email]
    }

    private fun clearTempUser(email: String) {
        tempUsers.remove(email)
        Log.d("AuthViewModel", "Cleared temp user: $email")
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: FirebaseUser? = null
)