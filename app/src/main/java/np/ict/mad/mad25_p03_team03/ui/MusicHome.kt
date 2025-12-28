// ui/MusicHome.kt
package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.navigation.AppNavGraph

@Composable
fun MusicHome(navController: NavHostController, songRepository: SongRepository,onSignOut: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController, currentDestination = currentDestination)
        }
    ) { paddingValues ->
        AppNavGraph(
            navController = navController, // 👈 确保 AppNavGraph 接收这个参数
            modifier = Modifier.padding(paddingValues),
            songRepository = songRepository,
            onSignOut = onSignOut
        )
    }
}