package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.repo.cam.RecordingStateStore
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import javax.inject.Inject
import kotlin.time.ExperimentalTime

class LogoutCamera @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource,
    private val camManager: CamManager,
    private val recordingStateStore: RecordingStateStore
) : Interactor<Unit>() {

    @ExperimentalTime
    override suspend fun doWork(params: Unit) = withContext(Dispatchers.IO) {
        try {
            dataSource.logout()
        } catch (ignore: Throwable) {
        }
        recordingStateStore.store().clear("1")
        camManager.onLogout()
    }
}
