package np.ict.mad.mad25_p03_team03.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import np.ict.mad.mad25_p03_team03.data.SimpleSong

class FirestoreService {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getSongs(): List<SimpleSong> {
        return try {
            val snapshot = firestore.collection("songs-zy").get().await()
            snapshot.toObjects(SimpleSong::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}