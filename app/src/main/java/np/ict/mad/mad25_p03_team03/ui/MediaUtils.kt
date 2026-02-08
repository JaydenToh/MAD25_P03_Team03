package np.ict.mad.mad25_p03_team03

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

// Function - Composable - Lifecycle manager for Media3 Controller
// Flow 1.0: Entry Point
@Composable
fun rememberMediaController(): MediaController? {
    // Flow 1.1: Dependency Setup
    val context = LocalContext.current

    // Variable - State - Holds the connected controller instance
    // Flow 1.2: State Initialization
    var controller by remember { mutableStateOf<MediaController?>(null) }

    // Lifecycle - Side Effect - Handles connection setup and cleanup
    // Flow 2.0: Effect Trigger
    DisposableEffect(context) {

        // Variable - Token - Identifies the PlaybackService to bind to
        // Flow 2.1: Service Token Creation
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))

        // Variable - Future - Asynchronous task to build the controller
        // Flow 2.2: Async Build Start
        val controllerFuture: ListenableFuture<MediaController> =
            MediaController.Builder(context, sessionToken).buildAsync()

        // Logic - Listener - Waits for the controller build to complete
        // Flow 2.3: Listener Attachment
        controllerFuture.addListener({
            try {
                // Flow 2.4: Success Handling
                controller = controllerFuture.get()
            } catch (e: Exception) {
                // Flow 2.5: Error Handling
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor()) // Flow 2.6: Execution Thread

        // Logic - Cleanup - Releases resources when Composable is disposed
        // Flow 3.0: Cleanup Trigger
        onDispose {
            // Flow 3.1: Future Management
            controllerFuture.let {
                // Flow 3.2: Cancel Pending Task
                if (!it.isDone) {
                    it.cancel(true)
                }
            }

            // Flow 3.3: Resource Release
            controller?.release()
        }
    }
    // Flow 4.0: Return Instance
    return controller
}