// navigation/AppRoot.kt
package np.ict.mad.mad25_p03_team03.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import np.ict.mad.mad25_p03_team03.LoginScreen
//import np.ict.mad.mad25_p03_team03.OTPScreen
import np.ict.mad.mad25_p03_team03.SignUpScreen
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.ui.*

// Function - Composable - Root entry point for app-level navigation and authentication state
// Flow 1.0: Screen Entry Point
@Composable
fun AppRoot() {
    // Variable - Service - Firebase Authentication instance
    // Flow 1.1: Dependency Setup
    val auth = FirebaseAuth.getInstance()

    // Variable - State - Tracks the current root-level screen to display
    // Flow 1.2: State Initialization
    var currentScreen by remember { mutableStateOf<RootScreen>(RootScreen.Login) }

    // Variable - Repository - Shared instance for song data fetching
    // Flow 1.3: Repository Initialization
    val songRepository = remember { SongRepository() }

    // Logic - Side Effect - Checks if a user session exists on startup
    // Flow 2.0: Auto-Login Check
    LaunchedEffect(Unit) {
        // Flow 2.1: Session Verification
        if (auth.currentUser != null) {
            // Flow 2.2: State Update to Music
            currentScreen = RootScreen.Music
        }
    }

    // Logic - Selection - Renders the UI based on the current root screen state
    // Flow 3.0: Root Screen Switching
    when (currentScreen) {
        // Flow 3.1: Login Screen Rendering
        is RootScreen.Login -> LoginScreen(
            // Flow 3.2: Navigation to SignUp
            onSignUpClick = { currentScreen = RootScreen.SignUp },
            // Flow 3.3: Navigation to OTP after Login
            onLoginSuccess = { currentScreen = RootScreen.Music }
        )

        // Flow 3.4: SignUp Screen Rendering
        is RootScreen.SignUp -> SignUpScreen(
            // Flow 3.5: Navigation back to Login
            onBackToLoginClick = { currentScreen = RootScreen.Login }
        )

        is RootScreen.Music -> {
            MusicRoot(
                // Variable - Input - Passing the song repository
                songRepository = songRepository,
                // Flow 3.9: Sign Out Logic
                onSignOut = {
                    auth.signOut()
                    currentScreen = RootScreen.Login
                }
            )
        }
    }
}

// Class - Sealed - Defines the available root navigation destinations
// Flow 4.0: Navigation Graph Definition
sealed class RootScreen {
    object Login : RootScreen()
    object SignUp : RootScreen()
    object Music : RootScreen()
}