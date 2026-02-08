package np.ict.mad.mad25_p03_team03

import android.Manifest
import android.app.Activity
import android.content.Context
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


private const val USE_FAKE_IDENTIFY_RESULT = true

// Fake results for quick UI testing
private val fakeIdentifyResults = listOf(
    "Song: Blinding Lights\nArtist: The Weeknd",
    "Song: Shape of You\nArtist: Ed Sheeran",
    "Song: Levitating\nArtist: Dua Lipa",
    "Song: Stay\nArtist: The Kid LAROI & Justin Bieber",
    "Song: Perfect\nArtist: Ed Sheeran"
)

// Colour palette
private val DarkBackground1 = Color(0xFF121212)
private val CardColor1 = Color(0xFF2F2F45)

// Compose UI state
@Composable
fun SongIdentifier() {
    val context = LocalContext.current
    val activity = context as? Activity
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val sectionNav = rememberNavController()

    NavHost(
        navController = sectionNav,
        startDestination = "identifier_main"
    ) {
        composable("identifier_main") {
            // Ensure history is loaded from storage
            LaunchedEffect(Unit) {
                IdentifiedSongHistory.ensureLoaded(context)
            }

            var isRecording by remember { mutableStateOf(false) }
            var statusText by remember { mutableStateOf("Tap the music note to start") }
            var songText by remember { mutableStateOf("") }
            var selectedMood by remember { mutableStateOf<String?>(null) }

            // Recording state
            val mediaRecorderState = remember { mutableStateOf<MediaRecorder?>(null) }
            val audioFileState = remember { mutableStateOf<File?>(null) }

            fun applyState(rec: Boolean, status: String, song: String) {
                isRecording = rec
                statusText = status

                if (song.isNotBlank()) {
                    songText = song
                    IdentifiedSongHistory.addFromSongText(songText, selectedMood)
                    IdentifiedSongHistory.saveToPreferences(context)
                } else {
                    songText = song
                }
            }

            // Request for microphone permission
            val requestMicPermissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        startRecording(
                            context = context,
                            mediaRecorderState = mediaRecorderState,
                            audioFileState = audioFileState
                        ) { rec, status, song ->
                            applyState(rec, status, song)
                        }
                    } else {
                        Toast.makeText(context, "Microphone permission required", Toast.LENGTH_SHORT).show()
                    }
                }

            fun checkPermissionAndStart() {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    startRecording(
                        context = context,
                        mediaRecorderState = mediaRecorderState,
                        audioFileState = audioFileState
                    ) { rec, status, song ->
                        applyState(rec, status, song)
                    }
                } else {
                    requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            fun handleButtonClick() {
                if (isRecording) {
                    stopRecording(
                        mediaRecorderState = mediaRecorderState,
                        audioFile = audioFileState.value
                    ) { rec, status, song ->
                        applyState(rec, status, song)
                    }
                } else {
                    selectedMood = null
                    songText = ""
                    checkPermissionAndStart()
                }
            }

            SongIdentifierScreen(
                isRecording = isRecording,
                statusText = statusText,
                songText = songText,
                selectedMood = selectedMood,
                onButtonClick = { handleButtonClick() },
                onBackClick = { backDispatcher?.onBackPressed() ?: activity?.finish() },
                onHistoryClick = { sectionNav.navigate("identifier_history") },
                onIdentifierClick = { },
                onMoodPlaylistClick = { sectionNav.navigate("mood_playlist") },
                onMoodSelected = { mood ->
                    selectedMood = mood
                    IdentifiedSongHistory.updateLastMood(mood)
                    IdentifiedSongHistory.saveToPreferences(context)
                }
            )
        }

        composable("identifier_history") {
            IdentifierHistoryScreen(
                onBackClick = { sectionNav.popBackStack() },
                onHistoryClick = { },
                onIdentifierClick = { sectionNav.navigate("identifier_main") },
                onMoodPlaylistClick = { sectionNav.navigate("mood_playlist") }
            )
        }

        composable("mood_playlist") {
            MoodPlaylistScreen(
                onBackClick = { sectionNav.popBackStack() },
                onHistoryClick = { sectionNav.navigate("identifier_history") },
                onIdentifierClick = { sectionNav.navigate("identifier_main") },
                onMoodPlaylistClick = { }
            )
        }
    }
}

// Configure it to start Recording and capturing audio
private fun startRecording(
    context: Context,
    mediaRecorderState: MutableState<MediaRecorder?>,
    audioFileState: MutableState<File?>,
    onStateChange: (Boolean, String, String) -> Unit
) {
    try {
        // Use of LLM to get the code to store the audio recording in a external file directory
        val file = File(context.getExternalFilesDir(null), "temp_audio.m4a")
        audioFileState.value = file

        val recorder = MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        mediaRecorderState.value = recorder

        onStateChange(true, "Listening…", "")

        // Auto-stop after 8 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            if (mediaRecorderState.value != null) {
                stopRecording(mediaRecorderState, audioFileState.value, onStateChange)
            }
        }, 8000)

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to start recording", Toast.LENGTH_SHORT).show()
        onStateChange(false, "Tap the music note to start", "Failed to start recording.")
    }
}

private fun stopRecording(
    mediaRecorderState: MutableState<MediaRecorder?>,
    audioFile: File?,
    onStateChange: (Boolean, String, String) -> Unit
) {
    try {
        mediaRecorderState.value?.apply {
            stop()
            release()
        }
    } catch (_: Exception) {
    }
    mediaRecorderState.value = null
    onStateChange(false, "Processing audio…", "")

    val file = audioFile
    if (file == null || !file.exists() || file.length() <= 1000) {
        onStateChange(false, "Tap the music note to start", "Recording failed or was too short.")
        return
    }

    // Fake mode for testing UI without using real API
    if (USE_FAKE_IDENTIFY_RESULT) {
        val fake = fakeIdentifyResults.random()
        onStateChange(false, "Tap the music note to identify another song", fake)
        return
    }

    // Send audio file to AudD API for song recognition
    uploadToAudD(file, onStateChange)
}

private fun uploadToAudD(
    file: File,
    onStateChange: (Boolean, String, String) -> Unit
) {

    val apiKeyRequest = "2163f30c6fae631c67eb719dd37c03a1"
        .toRequestBody("text/plain".toMediaTypeOrNull())

    val audioRequest = file.asRequestBody("audio/*".toMediaTypeOrNull())
    val audioPart = MultipartBody.Part.createFormData("file", file.name, audioRequest)

    RetrofitClient.apiService.identifySong(apiKeyRequest, audioPart)
        .enqueue(object : Callback<SongResponse> {

            override fun onResponse(
                call: Call<SongResponse>,
                response: Response<SongResponse>
            ) {
                if (!response.isSuccessful) {
                    onStateChange(false, "Tap the music note to start", "API error: ${response.code()}")
                    return
                }

                val result = response.body()?.result
                if (result != null) {
                    onStateChange(
                        false,
                        "Tap the music note to identify another song",
                        "Song: ${result.title}\nArtist: ${result.artist}"
                    )
                } else {
                    onStateChange(
                        false,
                        "No match found please try again",
                        ""
                    )
                }
            }

            override fun onFailure(call: Call<SongResponse>, t: Throwable) {
                onStateChange(false, "Tap the music note to try again", "Network failure: ${t.message}")
            }
        })
}

// ---------------- UI ----------------

@Composable
fun SongIdentifierScreen(
    isRecording: Boolean,
    statusText: String,
    songText: String,
    selectedMood: String?,
    onButtonClick: () -> Unit,
    onBackClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onIdentifierClick: () -> Unit,
    onMoodPlaylistClick: () -> Unit,
    onMoodSelected: (String) -> Unit
) {
    // UI colors
    val primaryText = Color.White
    val secondaryText = Color.White.copy(alpha = 0.78f)

    val tabSelected = Color.White.copy(alpha = 0.16f)
    val tabUnselectedText = Color.White.copy(alpha = 0.72f)
    val tabContainer = CardColor1.copy(alpha = 0.55f)

    val moodSelected = Color.White.copy(alpha = 0.16f)
    val moodUnselected = Color.White.copy(alpha = 0.08f)

    var showResultCard by remember { mutableStateOf(false) }
    LaunchedEffect(songText) {
        showResultCard = songText.isNotBlank()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground1)
    ) {
        val isSmallPhone = maxHeight < 760.dp

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 25.dp, bottom = 16.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryText
                        )
                    }
                    Text(
                        text = "Song Identifier",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(tabContainer, RoundedCornerShape(18.dp))
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { onHistoryClick() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "History",
                                fontSize = 13.sp,
                                color = tabUnselectedText,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(tabSelected, RoundedCornerShape(14.dp))
                                .clickable { onIdentifierClick() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Identifier",
                                fontSize = 13.sp,
                                color = primaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { onMoodPlaylistClick() }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Playlist",
                                fontSize = 13.sp,
                                color = tabUnselectedText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Hold your phone near the music source",
                    fontSize = 15.sp,
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Main button area
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Button(
                        onClick = onButtonClick,
                        shape = CircleShape,
                        modifier = Modifier.size(230.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            CardColor1,
                                            CardColor1.copy(alpha = 0.98f),
                                            DarkBackground1
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color.White.copy(alpha = 0.11f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRecording) {
                                Text(
                                    text = "STOP SEARCHING",
                                    color = primaryText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Identify music",
                                    tint = primaryText,
                                    modifier = Modifier.size(78.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = statusText,
                        fontSize = 16.sp,
                        color = primaryText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                    )
                }

                if (!isSmallPhone) {
                    // Slide-up result card
                    AnimatedVisibility(
                        visible = showResultCard,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        ResultCard(
                            songText = songText,
                            selectedMood = selectedMood,
                            moodSelected = moodSelected,
                            moodUnselected = moodUnselected,
                            primaryText = primaryText,
                            secondaryText = secondaryText,
                            onMoodSelected = onMoodSelected,
                            onClose = { showResultCard = false }
                        )
                    }
                }
            }

            if (isSmallPhone) {
                AnimatedVisibility(
                    visible = showResultCard,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    ResultCard(
                        songText = songText,
                        selectedMood = selectedMood,
                        moodSelected = moodSelected,
                        moodUnselected = moodUnselected,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        onMoodSelected = onMoodSelected,
                        onClose = { showResultCard = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    songText: String,
    selectedMood: String?,
    moodSelected: Color,
    moodUnselected: Color,
    primaryText: Color,
    secondaryText: Color,
    onMoodSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp, max = 320.dp)
            .padding(horizontal = 8.dp, vertical = 22.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CardColor1,
                        CardColor1.copy(alpha = 0.85f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // left placeholder (same width as the close button)
                Box(modifier = Modifier.size(40.dp))

                Text(
                    text = "We Found This Song!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = primaryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = songText,
                fontSize = 19.sp,
                color = primaryText,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "What mood do you think this song fits best?",
                fontSize = 14.sp,
                lineHeight = 22.sp,
                color = secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            val moods = listOf("Chill", "Hype", "Emotional")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                moods.forEach { mood ->
                    Button(
                        onClick = { onMoodSelected(mood) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMood == mood) moodSelected else moodUnselected
                        ),
                        shape = RoundedCornerShape(999.dp),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = mood,
                            fontSize = 13.sp,
                            color = primaryText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
