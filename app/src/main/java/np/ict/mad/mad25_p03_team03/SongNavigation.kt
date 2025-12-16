package np.ict.mad.mad25_p03_team03

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.graphics.vector.ImageVector



enum class MusicDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    LIBRARY("library", "Library", Icons.Filled.LibraryMusic),
    IDENTIFY("identify", "Identify", Icons.Filled.Mic),
    FAVORITES("favorites", "Favorites", Icons.Filled.Favorite)
}

