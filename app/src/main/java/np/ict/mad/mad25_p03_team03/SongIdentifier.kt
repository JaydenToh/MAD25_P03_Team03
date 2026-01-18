package np.ict.mad.mad25_p03_team03

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.History
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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

// Toggle this flag for testing without real API
private const val USE_FAKE_IDENTIFY_RESULT = true

// Fake results for quick UI testing
private val fakeIdentifyResults = listOf(
    "Song: Blinding Lights\nArtist: The Weeknd",
    "Song: Shape of You\nArtist: Ed Sheeran",
    "Song: Levitating\nArtist: Dua Lipa",
    "Song: Stay\nArtist: The Kid LAROI & Justin Bieber",
    "Song: Perfect\nArtist: Ed Sheeran"
)

// Compose UI state
@Composable
fun SongIdentifier() {
    val context = LocalContext.current
    val activity = context as? Activity

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
        onBackClick = { activity?.finish() },
        onHistoryClick = {
            context.startActivity(
                Intent(context, IdentifierHistory::class.java)
            )
        },
        onIdentifierClick = {
            // Already on this screen
        },
        onMoodPlaylistClick = {
            context.startActivity(
                Intent(context, MoodPlaylist::class.java)
            )
        },
        onMoodSelected = { mood ->
            selectedMood = mood
            IdentifiedSongHistory.updateLastMood(mood)
            IdentifiedSongHistory.saveToPreferences(context)
        }
    )
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
    // Send audio file to AudD API for song recognition
    uploadToAudD(file, onStateChange)
}

private fun uploadToAudD(
    file: File,
    onStateChange: (Boolean, String, String) -> Unit
) {
    // Fake mode for testing UI without using real API
    if (USE_FAKE_IDENTIFY_RESULT) {
        val fake = fakeIdentifyResults.random()
        onStateChange(
            false,
            "Tap the music note to identify another song",
            fake
        )
        return
    }

    val apiKeyRequest = "15917ddb670bc03f22799efae908c24a"     // New API Key to access AudD API
        .toRequestBody("text/plain".toMediaTypeOrNull())

    val audioRequest = file.asRequestBody("audio/*".toMediaTypeOrNull())
    val audioPart = MultipartBody.Part.createFormData("file", file.name, audioRequest)

    RetrofitClient.apiService.identifySong(apiKeyRequest, audioPart)         // Call AudD identify endpoint via Retrofit
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
                        "Tap the music note to identify another song", // If Success: show song title and artist
                        "Song: ${result.title}\nArtist: ${result.artist}"
                    )
                } else {
                    onStateChange(
                        false,
                        "No match found please try again", // API responded but could not recognize the song
                        ""
                    )
                }
            }

            // Will be called when network request itself fails or timeout
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF59168B),
                        Color(0xFF312C85),
                        Color(0xFF1C398E)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 60.dp,
                    bottom = 40.dp
                )
        ) {
            // Top row with back arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Song Identifier",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 40.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hold your phone near the music source",
                    fontSize = 16.sp,
                    color = Color(0xCCFFFFFF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onButtonClick,
                    shape = CircleShape,
                    modifier = Modifier.size(230.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF4C6FFF),
                                        Color(0xFF3A7BD5),
                                        Color(0xFF1E2C6F)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRecording) {
                            Text(
                                text = "STOP SEARCHING",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Identify music",
                                tint = Color.White,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = statusText,
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 32.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Slide-up result card
            AnimatedVisibility(
                visible = songText.isNotBlank(),
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp)
                        .padding(horizontal = 8.dp, vertical = 45.dp)
                        .background(
                            color = Color(0xAA000000),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 22.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "We Found This Song",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xCCFFFFFF)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = songText,
                            fontSize = 22.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 26.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Question text above the mood buttons
                        Text(
                            text = "What mood do you think this song fits best?",
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                            color = Color(0xCCFFFFFF),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Mood selection row
                        val moods = listOf("Chill", "Hype", "Emotional")

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            moods.forEach { mood ->
                                Button(
                                    onClick = { onMoodSelected(mood) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedMood == mood)
                                            Color(0xFF4C6FFF)
                                        else
                                            Color(0x33FFFFFF)
                                    ),
                                    contentPadding = PaddingValues(
                                        vertical = 10.dp,
                                        horizontal = 12.dp
                                    )
                                ) {
                                    Text(
                                        text = mood,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ------- Bottom Navigation --------
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(vertical = 5.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // History
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onHistoryClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "History",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                // Song Identifier Page
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onIdentifierClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Identifier",
                        tint = Color(0xFF4C6FFF),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Identifier",
                        fontSize = 12.sp,
                        color = Color(0xFF4C6FFF),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Playlist
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onMoodPlaylistClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = "Playlist",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Playlist",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
