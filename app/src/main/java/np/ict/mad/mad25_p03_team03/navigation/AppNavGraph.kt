// navigation/AppNavGraph.kt
package np.ict.mad.mad25_p03_team03.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import np.ict.mad.mad25_p03_team03.SongIdentifier
import np.ict.mad.mad25_p03_team03.SongLibraryScreen
import np.ict.mad.mad25_p03_team03.data.Difficulty
import np.ict.mad.mad25_p03_team03.data.GameMode
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.ui.BottomNavBar
import np.ict.mad.mad25_p03_team03.ui.ChatScreen
import np.ict.mad.mad25_p03_team03.ui.FriendListScreen
import np.ict.mad.mad25_p03_team03.ui.GameScreen
import np.ict.mad.mad25_p03_team03.ui.HomeScreen
import np.ict.mad.mad25_p03_team03.ui.LeaderboardScreen
import np.ict.mad.mad25_p03_team03.ui.LobbyScreen
import np.ict.mad.mad25_p03_team03.ui.ModeSelectionScreen
import np.ict.mad.mad25_p03_team03.ui.PlayerProfileScreen
import np.ict.mad.mad25_p03_team03.ui.ProfileScreen
import np.ict.mad.mad25_p03_team03.ui.PvpGameScreen
import np.ict.mad.mad25_p03_team03.ui.RulesScreen
import np.ict.mad.mad25_p03_team03.ui.SongCategoryScreen
import np.ict.mad.mad25_p03_team03.ui.SongDetailScreen
import np.ict.mad.mad25_p03_team03.ui.findOrCreateGame
import kotlinx.coroutines.launch
import np.ict.mad.mad25_p03_team03.ui.MimicGameScreen
import np.ict.mad.mad25_p03_team03.ui.MultiplayerMimicGameScreen
import np.ict.mad.mad25_p03_team03.ui.MultiplayerModeSelectionScreen
import np.ict.mad.mad25_p03_team03.ui.RhythmGameScreen

// navigation/AppNavGraph.kt
@Composable
fun AppNavGraph(navController: NavHostController,
                modifier: Modifier = Modifier,
                songRepository: SongRepository,
                onSignOut: () -> Unit) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination


        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = modifier
        ) {
            composable("home") {
                HomeScreen(
                    onStartGame = { navController.navigate("rules") },
                    onOpenLobby = { navController.navigate("lobby") },
                    onIdentifySong = { navController.navigate("identifier") },
                    onSignOut = onSignOut,
                    onOpenMimic = { navController.navigate("mimic_game") },
                )
            }

            composable("lobby") {
                LobbyScreen(
                    songRepository = songRepository,
                    onNavigateToCreate = {
                        navController.navigate("multiplayer_mode_select")
                    },
                    onNavigateToGame = { roomId,gameType ->
                        when (gameType) {
                            "RHYTHM" -> navController.navigate("rhythm_game/$roomId")
                            "MIMIC" -> navController.navigate("mimic_game/$roomId")
                            else -> navController.navigate("pvp_game/$roomId")
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("multiplayer_mode_select") {
                val context = LocalContext.current
                val db = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                val scope = rememberCoroutineScope()

                MultiplayerModeSelectionScreen(
                    onBack = { navController.popBackStack() },
                    onCreateRoom = { selectedMode,selectedType, isBotMode ->

                        if (currentUser != null) {
                            Toast.makeText(context, "Creating room...", Toast.LENGTH_SHORT).show()

                            scope.launch {

                                val songs = songRepository.fetchSongsFromSupabase(selectedMode).take(10)

                                val mappedQuestions = songs.map { song ->
                                    val options = (listOf(song.title) + song.fakeOptions).shuffled().take(4)
                                    mapOf(
                                        "correctTitle" to song.title,
                                        "options" to options,
                                        "audioUrl" to song.audioUrl
                                    )
                                }


                                val username = currentUser.email?.substringBefore("@") ?: "Player"
                                val newRoom = hashMapOf(
                                    "player1Id" to currentUser.uid,
                                    "player1Name" to username,
                                    "player2Id" to null,
                                    "player2Name" to if (isBotMode) "🤖 AI Bot" else null,
                                    "status" to if (isBotMode) "playing" else "waiting",
                                    "gameMode" to selectedMode.name,
                                    "gameType" to selectedType.name,
                                    "createdAt" to com.google.firebase.Timestamp.now(),
                                    "currentQuestionIndex" to 0,
                                    "ballPosition" to 0,
                                    "questions" to mappedQuestions
                                )


                                db.collection("pvp_rooms").add(newRoom)
                                    .addOnSuccessListener { docRef ->

                                        when (selectedType) {
                                            np.ict.mad.mad25_p03_team03.ui.GameType.RHYTHM ->
                                                navController.navigate("rhythm_game/${docRef.id}")
                                            np.ict.mad.mad25_p03_team03.ui.GameType.MIMIC ->
                                                navController.navigate("mimic_game/${docRef.id}")
                                            else ->
                                                navController.navigate("pvp_game/${docRef.id}")
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to create room", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                )
            }

            composable(
                route = "mimic_game/{roomId}",
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                MultiplayerMimicGameScreen(
                    roomId = roomId,
                    onNavigateBack = {
                        navController.navigate("lobby") { popUpTo("lobby") { inclusive = true } }
                    }
                )
            }

            composable(
                route = "rhythm_game/{roomId}",
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                RhythmGameScreen(
                    roomId = roomId,
                    songRepository = songRepository,
                    onNavigateBack = { navController.navigate("lobby") { popUpTo("lobby") { inclusive = true } } }
                )
            }

            composable("rules") {
                RulesScreen(
                    onStartGame = { navController.navigate("mode_selection") },
                    onBack = { navController.popBackStack() }
                )
            }

            composable("mode_selection") {
                val context = LocalContext.current
                val db = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val scope = rememberCoroutineScope()

                ModeSelectionScreen(
                    onStartGame = { mode, difficulty ->
                        navController.navigate("game/${mode.name}/${difficulty.name}")
                    },
                    onStartPvp = {
                        val user = auth.currentUser
                        if (user != null) {
                            Toast.makeText(context, "Finding match...", Toast.LENGTH_SHORT).show()

                            findOrCreateGame(
                                db = db,
                                currentUser = user,
                                songRepository = songRepository,
                                onGameFound = { roomId ->
                                    navController.navigate("pvp_game/$roomId")
                                },
                                onFail = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "game/{mode}/{difficulty}",
                arguments = listOf(
                    navArgument("mode") { type = NavType.StringType },
                    navArgument("difficulty") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val modeString = backStackEntry.arguments?.getString("mode") ?: GameMode.ENGLISH.name
                val mode = try { GameMode.valueOf(modeString) } catch (e: Exception) { GameMode.ENGLISH }


                val diffString = backStackEntry.arguments?.getString("difficulty") ?: Difficulty.EASY.name
                val difficulty = try { Difficulty.valueOf(diffString) } catch (e: Exception) { Difficulty.EASY }

                GameScreen(
                    songRepository = songRepository,
                    gameMode = mode,
                    difficulty = difficulty,
                    onNavigateBack = { navController.popBackStack() }
                )
            }


            composable("identifier") {
                SongIdentifier()
            }

            composable("mimic_game") {
                MimicGameScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("leaderboard") {
                LeaderboardScreen(onPlayerClick = { userId ->
                    navController.navigate("player_profile/$userId")
                })
            }

            composable(
                route = "player_profile/{userId}",
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                PlayerProfileScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("profile") {
                ProfileScreen(onViewFriends = { navController.navigate("friend_list") })
            }

            composable("friend_list") {
                FriendListScreen(
                    onBack = { navController.popBackStack() },onChatClick = { friendId, friendName ->
                        navController.navigate("chat/$friendId/$friendName")
                    }
                )
            }

            composable(
                route = "chat/{friendId}/{friendName}",
                arguments = listOf(
                    navArgument("friendId") { type = NavType.StringType },
                    navArgument("friendName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
                val friendName = backStackEntry.arguments?.getString("friendName") ?: "Friend"

                ChatScreen(
                    friendId = friendId,
                    friendName = friendName,
                    onBack = { navController.popBackStack() }
                )
            }


            composable("library") {
                SongCategoryScreen(
                    onCategorySelected = { collectionName ->

                        navController.navigate("library_list/$collectionName")
                    },
                    onBack = { navController.popBackStack() }
                )
            }


            composable(
                route = "library_list/{collectionName}",
                arguments = listOf(navArgument("collectionName") { type = NavType.StringType })
            ) { backStackEntry ->

                val collectionName = backStackEntry.arguments?.getString("collectionName") ?: "songs"

                SongLibraryScreen(
                    collectionName = collectionName,
                    onNavigateBack = { navController.popBackStack() },
                    onSongClick = { songTitle ->
                        navController.navigate("song_detail/$collectionName/$songTitle")
                    }
                )
            }

            composable(
                route = "song_detail/{collectionName}/{songTitle}",
                arguments = listOf(
                    navArgument("collectionName") { type = NavType.StringType },
                    navArgument("songTitle") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val collectionName = backStackEntry.arguments?.getString("collectionName") ?: ""
                val songTitle = backStackEntry.arguments?.getString("songTitle") ?: ""

                SongDetailScreen(
                    collectionName = collectionName,
                    songTitle = songTitle,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "pvp_game/{roomId}",
                arguments = listOf(navArgument("roomId") { type = NavType.StringType })
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getString("roomId") ?: ""


                PvpGameScreen(
                    roomId = roomId,
                    songRepository = songRepository,
                    onNavigateBack = {

                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
