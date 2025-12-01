/*import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFirebaseService(): FirebaseService {
        return FirebaseService()
    }

    @Provides
    @Singleton
    fun provideSongRepository(firebaseService: FirebaseService): np.ict.mad.mad25_p03_team03.data.repository.SongRepository {
        return np.ict.mad.mad25_p03_team03.data.repository.SongRepository(firebaseService)
    }
}*/