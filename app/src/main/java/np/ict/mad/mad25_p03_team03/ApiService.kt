package np.ict.mad.mad25_p03_team03

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @Multipart
    @POST("/")
    fun identifySong(
        @Part("api_token") apiToken: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<SongResponse>
}




