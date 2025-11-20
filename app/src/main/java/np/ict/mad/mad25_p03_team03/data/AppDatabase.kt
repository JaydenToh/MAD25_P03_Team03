package np.ict.mad.mad25_p03_team03.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import np.ict.mad.mad25_p03_team03.R

@Database(entities = [SongEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun songDao(): SongDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "song_database"
                )
                    .build()

                INSTANCE = instance

                // ⭐ App 启动后自动检查是否需要插入 dummy data
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = instance.songDao()

                    if (dao.getAllSongs().isEmpty()) {
                        dao.insertSongs(
                            listOf(
                                SongEntity(
                                    title = "Song A",
                                    artist = "Artist 1",
                                    audioResId = R.raw.song1
                                ),
                                SongEntity(
                                    title = "Song B",
                                    artist = "Artist 2",
                                    audioResId = R.raw.song2
                                ),
                                SongEntity(
                                    title = "Song C",
                                    artist = "Artist 3",
                                    audioResId = R.raw.song3
                                )
                            )
                        )
                    }

                }

                instance
            }
        }
    }
}
