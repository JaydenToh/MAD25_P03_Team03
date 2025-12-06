package np.ict.mad.mad25_p03_team03.data

import android.util.Log
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.ict.mad.mad25_p03_team03.data.remote.dto.SongDto

class SongRepository {

    // Initialize Supabase client
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://qxttrbahplhiqdxxirbb.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF4dHRyYmFocGxoaXFkeHhpcmJiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ5MTQ3OTAsImV4cCI6MjA4MDQ5MDc5MH0.QIhpVaO4XC-m_fz-2LCu4tZ2UPi1aJDcajYcSNvsrOc" // Keep your key here
    ) {
        install(Postgrest)
    }

    suspend fun fetchSongsFromSupabase(): List<SongDto> {
        return try {
            Log.d("SongRepo", "Fetching songs...")


            val songs = supabase.from("songs")
                .select(columns = Columns.list("id, title, artist, audio_url, fake_options"))
                .decodeList<SongDto>() // Decode directly to a list of SongDto

            Log.d("SongRepo", "Fetched ${songs.size} songs")
            songs
        } catch (e: Exception) {
            Log.e("SongRepo", "Error fetching songs", e)
            emptyList()
        }
    }
}