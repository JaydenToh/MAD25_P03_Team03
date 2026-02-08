// np/ict/mad/mad25_p03_team03/ui/HomeScreen.kt

package np.ict.mad.mad25_p03_team03.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartGame: () -> Unit,
    onOpenLobby: () -> Unit,
    onIdentifySong: () -> Unit,
    onSignOut:  () -> Unit,
    onOpenMimic: () -> Unit
) {
    Scaffold(
        containerColor = DarkBackground1
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(top = 48.dp) // Space from top
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = Color.White
                )
                Text(
                    text = "Music App",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Text(
                text = "Choose your adventure:",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item(span = { GridItemSpan(1) }) {
                    HomeCard(
                        Icons.Default.VideogameAsset,
                        title = "Guessing Game",
                        contentColor = Color.Black,
                        onClick = onStartGame
                    )
                }

                item(span = { GridItemSpan(1) }) {
                    HomeCard(
                        Icons.Default.Mic,
                        title = "Humming Challenge",
                        contentColor = Color.Black,
                        onClick = onOpenMimic
                    )
                }

                item(span = { GridItemSpan(1) }) {
                    HomeCard(
                        Icons.Default.Search,
                        title = "Song Identifier",
                        contentColor = Color.Black,
                        onClick = onIdentifySong
                    )
                }

                item(span = { GridItemSpan(1) }) {
                    HomeCard(
                        Icons.Default.Groups,
                        title = "Multiplayer Lobby",
                        contentColor = Color.Black,
                        onClick = onOpenLobby
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            TextButton(
                onClick = onSignOut,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Log Out",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "v2.0 • MAD25 P02 Team 02",
                fontSize = 16.sp,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}


@Composable
private fun HomeCard(
    icon: ImageVector,
    title: String,
    contentColor: Color = Color.White,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(155.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardColor1,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(45.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

