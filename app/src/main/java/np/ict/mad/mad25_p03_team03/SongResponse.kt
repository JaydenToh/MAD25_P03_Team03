package np.ict.mad.mad25_p03_team03

data class SongResponse(
    val status: String,
    val result: Result?
)

data class Result(
    val title: String,
    val artist: String
)

