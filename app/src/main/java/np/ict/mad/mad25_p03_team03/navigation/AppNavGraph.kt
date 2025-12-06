// navigation/AppNavGraph.kt
package np.ict.mad.mad25_p03_team03.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import np.ict.mad.mad25_p03_team03.SongIdentifier
import np.ict.mad.mad25_p03_team03.SongLibrary
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.ui.GameScreen
import np.ict.mad.mad25_p03_team03.ui.HomeScreen
import np.ict.mad.mad25_p03_team03.ui.RulesScreen


@Composable
fun AppNavGraph(songRepository: SongRepository) {

    val context = LocalContext.current

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("home") {
            HomeScreen(
                onStartGame = { navController.navigate("rules") },
                onOpenLibrary = {
                    context.startActivity(Intent(context, SongLibrary::class.java))
                },
                onIdentifySong = { navController.navigate("identifier") }

            )
        }

        composable("rules") {
            RulesScreen(
                onStartGame = { navController.navigate("game") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("game") {
            GameScreen(songRepository = songRepository, onNavigateBack = { navController.popBackStack() } )
        // try to pass songRepository to GameScreen
        }

        composable("identifier") {
            SongIdentifier()
        }






    }
}