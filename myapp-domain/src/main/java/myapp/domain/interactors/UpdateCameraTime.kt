package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import org.threeten.bp.Instant
import javax.inject.Inject

class UpdateCameraTime @Inject constructor(
    private val logger: Logger,
    private val camManager: CamManager,
    private val dataSource: CamDataSource
) : Interactor<Unit>() {


    suspend fun executeSync() = executeSync(Unit)

    override suspend fun doWork(params: Unit) = withContext(Dispatchers.IO) {
        dataSource.updateTime(timeSeconds = Instant.now().epochSecond)
        val cfg = dataSource.config().getOrThrow()
        camManager.updateConfig(cfg)
    }
}
