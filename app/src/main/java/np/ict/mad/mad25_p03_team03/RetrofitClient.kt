import np.ict.mad.mad25_p03_team03.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.audd.io/"

    // Retrofit client: uses Retrofit to create a reusable ApiService for calling the AudD API
    val apiService: ApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())  // JSON converter
        .build()
        .create(ApiService::class.java)
}


