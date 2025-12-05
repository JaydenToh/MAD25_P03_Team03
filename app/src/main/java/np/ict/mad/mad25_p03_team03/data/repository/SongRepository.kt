package np.ict.mad.mad25_p03_team03.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import np.ict.mad.mad25_p03_team03.data.remote.dto.SongDto

class SongRepository {

    // 1. Fix: Add the builder block { install(Postgrest) }
    private val supabase = createSupabaseClient(
        supabaseUrl = "https://qxttrbahplhiqdxxirbb.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF4dHRyYmFocGxoaXFkeHhpcmJiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ5MTQ3OTAsImV4cCI6MjA4MDQ5MDc5MH0.QIhpVaO4XC-m_fz-2LCu4tZ2UPi1aJDcajYcSNvsrOc" // Keep your key here
    ) {
        install(Postgrest)
    }

    suspend fun fetchSongsFromSupabase(): List<SongDto> = withContext(Dispatchers.IO) {
        try {
            // 2. Fix: Use decodeList<SongDto>() for type safety
            val songs = supabase.postgrest["songs"]
                .select(columns = io.github.jan.supabase.postgrest.query.Columns.list("id, title, artist, audio_url, fake_options"))
                .decodeList<SongDto>()

            songs
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}