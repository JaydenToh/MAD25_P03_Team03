package np.ict.mad.mad25_p03_team03

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.* 
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    AppRoot()
                }
            }
        }
    }
}

sealed class RootScreen {
    object Login : RootScreen()
    object SignUp : RootScreen()
    object OTP : RootScreen()
    object Music : RootScreen()
}

@Composable
fun AppRoot() {
    var currentScreen by remember { mutableStateOf<RootScreen>(RootScreen.Login) }

    val auth = FirebaseAuth.getInstance()

    // Auto-login if user is already authenticated
    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            currentScreen = RootScreen.OTP
        }
    }

    when (currentScreen) {

        is RootScreen.Login -> LoginScreen(
            onSignUpClick = { currentScreen = RootScreen.SignUp },
            onLoginSuccess = { currentScreen = RootScreen.OTP }
        )

        is RootScreen.SignUp -> SignUpScreen(
            onBackToLoginClick = { currentScreen = RootScreen.Login }
        )

        is RootScreen.OTP -> OTPScreen(
            onContinueToApp = { currentScreen = RootScreen.Music }
        )

        is RootScreen.Music -> MusicAppNavigation()
    }
}
