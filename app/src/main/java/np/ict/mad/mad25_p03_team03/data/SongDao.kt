package np.ict.mad.mad25_p03_team03.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy

@Dao
interface SongDao {

    @Insert
    suspend fun insertSong(song: SongEntity)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :keyword || '%'")
    suspend fun searchSongs(keyword: String): List<SongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)
}
