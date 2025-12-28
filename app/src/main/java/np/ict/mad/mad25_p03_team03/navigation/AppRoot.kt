// navigation/AppRoot.kt
package np.ict.mad.mad25_p03_team03.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import np.ict.mad.mad25_p03_team03.LoginScreen
import np.ict.mad.mad25_p03_team03.OTPScreen
import np.ict.mad.mad25_p03_team03.SignUpScreen
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.ui.*

@Composable
fun AppRoot() {
    val auth = FirebaseAuth.getInstance()
    var currentScreen by remember { mutableStateOf<RootScreen>(RootScreen.Login) }
    val songRepository = remember { SongRepository() }


    LaunchedEffect(Unit) {
        if (auth.currentUser != null) {
            currentScreen = RootScreen.Music
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

        is RootScreen.Music -> {
            MusicRoot(
                songRepository = songRepository, // 🔥 传进去
                onSignOut = { auth.signOut()
                    currentScreen = RootScreen.Login}
            )
        }
    }
}

sealed class RootScreen {
    object Login : RootScreen()
    object SignUp : RootScreen()
    object OTP : RootScreen()
    object Music : RootScreen()
}