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
            // 注意：通常要在 ViewModel 或 Activity 销毁时才 release controller，
            // 但在简单 Compose 导航中，可以在这里处理，或者为了保持连接不频繁断开，通常配合 ViewModel 使用。
            // 这里为了简单，我们让它跟随 Compose 生命周期，但在跳转页面时可能会短暂断开。
            // 更好的做法是将 controller 放在 MainActivity 或 ViewModel 中传递下来。
            controller?.release()
        }
    }
    return controller
}