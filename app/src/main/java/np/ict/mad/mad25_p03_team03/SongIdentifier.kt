package np.ict.mad.mad25_p03_team03

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

class SongIdentifier : ComponentActivity() {

    // Compose UI state
    private var isRecording by mutableStateOf(false)
    private var statusText by mutableStateOf("Tap the button to start listening")
    private var songText by mutableStateOf("")

    // Recording state
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null

    private val requestMicPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startRecording()
            } else {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                MusicAppNavigation()
                SongIdentifierScreen(
                    isRecording = isRecording,
                    statusText = statusText,
                    songText = songText,
                    onButtonClick = {
                        if (isRecording) {
                            stopRecording()
                        } else {
                            checkPermissionAndStart()
                        }
                    }
                )
            }
        }
    }



    private fun checkPermissionAndStart() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            startRecording()
        } else {
            requestMicPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startRecording() {
        try {
            audioFile = File(getExternalFilesDir(null), "temp_audio.m4a")

            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFile!!.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            statusText = "Listening…"


            // Auto-stop after 8 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                if (isRecording) stopRecording()
            }, 8000)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {

        }

        mediaRecorder = null
        isRecording = false
        statusText = "Processing audio…"


        val file = audioFile
        if (file == null || !file.exists() || file.length() <= 1000) {
            statusText = "Tap the button to start listening"
            songText = "Recording failed or was too short."
            return
        }

        uploadToAudD(file)
    }


//////////////////////////////// - Need API KEY - /////////////////////////////////////////////////////////////
    private fun uploadToAudD(file: File) {
        val apiKeyRequest = "MY API KEY"
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
                        statusText = "Tap the button to start listening"
                        songText = "API error: ${response.code()}"
                        return
                    }

                    val result = response.body()?.result
                    if (result != null) {
                        statusText = "Tap the button to identify another song"
                        songText = "Song: ${result.title}\nArtist: ${result.artist}"
                    } else {
                        statusText = "No match found please try again"

                    }
                }

                override fun onFailure(call: Call<SongResponse>, t: Throwable) {
                    statusText = "Tap the button to try again"
                    songText = "Network failure: ${t.message}"
                }
            })
    }
}

// ---------------- UI ----------------

@Composable
fun SongIdentifierScreen(
    isRecording: Boolean,
    statusText: String,
    songText: String,
    onButtonClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF59168B),
                        Color(0xFF1C398E),
                        Color(0xFF312C85)
                    )
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Song Identifier",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 150.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Hold your phone near the music source",
                    fontSize = 14.sp,
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
                    modifier = Modifier.size(220.dp),
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Identify music",
                                tint = Color.White,
                                modifier = Modifier.size(72.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = statusText,
                    fontSize = 16.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(top = 50.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = songText,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp)
            )
        }
    }
}
