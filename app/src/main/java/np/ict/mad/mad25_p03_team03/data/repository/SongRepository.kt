import np.ict.mad.mad25_p03_team03.data.SimpleSong
import np.ict.mad.mad25_p03_team03.data.FirestoreService

class SongRepository(
    private val firestoreService: FirestoreService
) {
    suspend fun getSongs(): List<SimpleSong> {
        return firestoreService.getSongs()
    }
}