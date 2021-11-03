package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.entities.KuCameraConfig
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.ResultInteractor
import myapp.util.Logger
import javax.inject.Inject

class LoadConfig @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource,
    private val camManager: CamManager,
) : ResultInteractor<Unit, KuCameraConfig>() {

    override suspend fun doWork(params: Unit): KuCameraConfig = withContext(Dispatchers.IO) {
        val cfg = dataSource.config().getOrThrow()
        camManager.updateConfig(cfg)
        cfg
    }
}
