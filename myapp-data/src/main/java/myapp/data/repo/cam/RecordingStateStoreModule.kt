package myapp.data.repo.cam

import com.dropbox.android.external.store4.Fetcher
import com.dropbox.android.external.store4.MemoryPolicy
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import myapp.data.entities.KuRecordingState
import myapp.data.repo.cam.api.CamDataSource
import javax.inject.Singleton
import kotlin.time.Duration


@InstallIn(SingletonComponent::class)
@Module
object RecordingStateStoreModule {
    @Singleton
    @Provides
    fun provideRecordingStateStore(dataSource: CamDataSource): RecordingStateStore {
        return RecordingStateStore(dataSource)
    }
}

class RecordingStateStore constructor(
    val dataSource: CamDataSource
) {
    private val store: Store<String, KuRecordingState> = StoreBuilder.from(
        fetcher = Fetcher.of { _: String ->
            dataSource.recordingState().getOrThrow()
        }
    ).cachePolicy(
        MemoryPolicy.builder<String, KuRecordingState>()
            .setMaxSize(1)
            .setExpireAfterWrite(Duration.seconds(5))
            .build()
    ).build()

    fun store() = this.store
}
