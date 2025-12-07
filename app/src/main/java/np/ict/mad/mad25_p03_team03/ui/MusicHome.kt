// ui/MusicHome.kt
package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.navigation.AppNavGraph

@Composable
fun MusicHome() {
    val songRepository = SongRepository() // Repository（Supabase + local fallback）

    // render the main navigation graph
    AppNavGraph(songRepository = songRepository)
}