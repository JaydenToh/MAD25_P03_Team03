import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await
import np.ict.mad.mad25_p03_team03.data.SimpleSong

class FirebaseService {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    // 简化版本 - 只获取歌曲标题和艺术家
    suspend fun getSongs(): List<SimpleSong> {
        return try {
            val snapshot = database.child("songs").get().await()
            Log.d("FirebaseService", "Snapshot exists: ${snapshot.exists()}")
            Log.d("FirebaseService", "Snapshot value: ${snapshot.value}")

            val songs = mutableListOf<SimpleSong>()

            snapshot.children.forEach { child ->
                Log.d("FirebaseService", "Processing child: ${child.key}")

                // 直接从快照中提取数据，避免类型转换问题
                val title = child.child("title").getValue<String>() ?: ""
                val artist = child.child("artist").getValue<String>() ?: ""

                val song = SimpleSong(
                    id = child.key ?: "",
                    title = title,
                    artist = artist
                )

                songs.add(song)
                Log.d("FirebaseService", "Added song: ${song.title} - ${song.artist}")
            }

            Log.d("FirebaseService", "Loaded ${songs.size} songs")
            songs
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error loading songs", e)
            emptyList()
        }
    }
}