// ui/MusicHome.kt
package np.ict.mad.mad25_p03_team03.ui

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.navigation.AppNavGraph

@Composable
fun MusicHome(navController: NavHostController, songRepository: SongRepository,onSignOut: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val context = LocalContext.current
    val playingSong = MusicManager.currentSong

    Scaffold(
        bottomBar = {
            Column {
                if (playingSong != null) {
                    Box(modifier = Modifier.clickable {
                        val intent = Intent(context, MusicProfile::class.java).apply {
                            putExtra("TITLE", playingSong.title)
                            putExtra("ARTIST", playingSong.artist)
                            putExtra("LYRICS", playingSong.lyrics)
                            putExtra("IMAGE_ID", playingSong.drawableId)
                        }
                        context.startActivity(intent)
                    }) {
                        // 5. PASS ALL PARAMETERS
                        BottomPlayerBar(
                            song = playingSong,
                            isPlaying = MusicManager.isPlaying,
                            onPlayPause = { MusicManager.playPause(context) },
                            onNext = { MusicManager.next(context) },
                            onPrevious = { MusicManager.previous(context) }
                        )
                    }
                }

                BottomNavBar(navController = navController, currentDestination = currentDestination)
            }
        }
    ) { paddingValues ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
            songRepository = songRepository,
            onSignOut = onSignOut
        )
    }
}