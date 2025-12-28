package np.ict.mad.mad25_p03_team03.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import np.ict.mad.mad25_p03_team03.data.SongRepository
import np.ict.mad.mad25_p03_team03.ui.MusicHome
import np.ict.mad.mad25_p03_team03.ui.NotificationBanner
import np.ict.mad.mad25_p03_team03.ui.NotificationData

@Composable
fun MusicRoot(songRepository: SongRepository, onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var notification by remember { mutableStateOf<NotificationData?>(null) }

    // 1. 全局消息监听
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            db.collection("chats")
                .whereArrayContains("participants", currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == DocumentChange.Type.MODIFIED) {
                            val data = change.document.data
                            val lastSenderId = data["lastSenderId"] as? String
                            val lastMessage = data["lastMessage"] as? String ?: "New Message"

                            if (lastSenderId != null && lastSenderId != currentUser.uid) {
                                // 弹出通知内容
                                notification = NotificationData(
                                    senderName = "Chat Message",
                                    message = lastMessage,
                                    chatRoomId = change.document.id,
                                    senderId = lastSenderId
                                )
                            }
                        }
                    }
                }
        }
    }

    // 自动消失逻辑
    LaunchedEffect(notification) {
        if (notification != null) {
            delay(3000)
            notification = null
        }
    }

    // 2. UI 叠加结构
    Box(modifier = Modifier.fillMaxSize()) {
        // 底层：你原本的音乐主页面逻辑
        MusicHome(navController = navController,songRepository = songRepository,onSignOut = onSignOut)

        // 顶层：横幅弹窗
        AnimatedVisibility(
            visible = notification != null,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp) // 先设置左右
                .padding(top = 16.dp)        // 再额外设置顶部
                .zIndex(99f)
        ) {
            notification?.let { notif ->
                NotificationBanner(
                    data = notif,
                    onClick = {
                        // 3. 🔥 实现精确跳转逻辑
                        // 我们需要解析 chatRoomId 来找到 friendId (假设 ID 格式是 uid1_uid2)
                        // 或者直接跳转到 friend_list 也是一种妥协方案

                        // 尝试解析 friendId
                        val ids = notif.chatRoomId.split("_")
                        val friendId = ids.find { it != currentUser?.uid }

                        if (friendId != null) {
                            // 跳转到聊天页面 (名字暂时传 "Chat")
                            navController.navigate("chat/$friendId/Chat")
                        } else {
                            // 解析失败兜底方案
                            navController.navigate("friend_list")
                        }

                        notification = null
                    },
                    onDismiss = { notification = null }
                )
            }
        }
    }
}