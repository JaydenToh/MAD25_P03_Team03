// 放在 ModeSelectionScreen.kt 或单独的文件
package np.ict.mad.mad25_p03_team03.ui

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import np.ict.mad.mad25_p03_team03.data.SongRepository

fun findOrCreateGame(
    db: FirebaseFirestore,
    currentUser: FirebaseUser,
    songRepository: SongRepository, // 传入 Repository 用来生成题目
    onGameFound: (String) -> Unit,
    onFail: (String) -> Unit
) {
    // 1. 先找有没有等待中的房间
    db.collection("pvp_rooms")
        .whereEqualTo("status", "waiting")
        .limit(1)
        .get()
        .addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                // ✅ A. 找到了房间 -> 加入 (Join)
                val room = snapshot.documents[0]
                val roomId = room.id

                // 防止自己进自己房间
                if (room.getString("player1Id") == currentUser.uid) {
                    onGameFound(roomId)
                    return@addOnSuccessListener
                }

                db.collection("pvp_rooms").document(roomId)
                    .update(
                        mapOf(
                            "player2Id" to currentUser.uid,
                            "status" to "playing" // 马上开始
                        )
                    )
                    .addOnSuccessListener { onGameFound(roomId) }
                    .addOnFailureListener { onFail("Failed to join room") }

            } else {
                // 🆕 B. 没找到 -> 创建新房间并生成题目 (Create)

                // 这里我们使用协程或者简单的回调来获取题目
                // 注意：SongRepository.fetchSongsFromSupabase 是 suspend 函数
                // 简单起见，我们假设你能在 CoroutineScope 里调用，或者 Repository 有 callback 版本
                // 这里演示假设有一个 fetchRandomQuestionsSync 或者在 UI 层级调用

                // 为了简单，我们先创建房间，题目留空，然后在 PvpGameScreen 只有 Player 1 生成题目？
                // 不，最好的办法是在这里生成。为了代码简洁，我们假定这里能拿到 songRepository 的数据。
                // ⚠️ 实际代码中，你应该在 LaunchedEffect 里调用这个，或者把这个函数变成 suspend function

                createRoomWithQuestions(db, currentUser, songRepository, onGameFound, onFail)
            }
        }
        .addOnFailureListener { onFail(it.message ?: "Error finding room") }
}

// 辅助函数：创建带题目的房间
private fun createRoomWithQuestions(
    db: FirebaseFirestore,
    currentUser: FirebaseUser,
    songRepository: SongRepository,
    onSuccess: (String) -> Unit,
    onFail: (String) -> Unit
) {
    // ⚠️ 注意：这需要运行在 CoroutineScope 中，或者 Repository 提供回调
    // 这里示意数据结构

    val newRoom = hashMapOf(
        "player1Id" to currentUser.uid,
        "player2Id" to null,
        "status" to "waiting",
        "currentQuestionIndex" to 0,
        "scores" to hashMapOf(currentUser.uid to 0),
        "roundWinnerId" to null,
        // 🆕 预留一个空数组，或者在这里填入 fetch 到的题目
        // 建议：为了不阻塞 UI，我们可以先创建房间，进去后再由 Player 1 填充题目
        "questions" to emptyList<Map<String, Any>>()
    )

    db.collection("pvp_rooms").add(newRoom)
        .addOnSuccessListener { docRef -> onSuccess(docRef.id) }
        .addOnFailureListener { onFail(it.message ?: "Failed to create") }
}