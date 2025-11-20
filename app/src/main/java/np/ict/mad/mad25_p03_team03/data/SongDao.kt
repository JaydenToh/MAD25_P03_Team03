package np.ict.mad.mad25_p03_team03.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SongDao {

    @Insert
    suspend fun insertSong(song: SongEntity)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :keyword || '%'")
    suspend fun searchSongs(keyword: String): List<SongEntity>
}
