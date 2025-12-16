package np.ict.mad.mad25_p03_team03

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

@Composable
fun rememberMediaController(): MediaController? {
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }

    DisposableEffect(context) {
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val controllerFuture: ListenableFuture<MediaController> =
            MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener({
            try {
                controller = controllerFuture.get()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())

        onDispose {
            controllerFuture.let {
                if (!it.isDone) {
                    it.cancel(true)
                }
            }

            controller?.release()
        }
    }
    return controller
}