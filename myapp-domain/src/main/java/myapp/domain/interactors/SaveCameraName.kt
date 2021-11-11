package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.preferences.KuCameraPreference
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import timber.log.Timber
import javax.inject.Inject

class SaveCameraName @Inject constructor(
    private val logger: Logger,
    private val camManager: CamManager,
    private val dataSource: CamDataSource
) : Interactor<SaveCameraName.Params>() {

    data class Params(val ip: String, val cameraName: String)

    suspend fun executeSync(ip: String, cameraName: String) = executeSync(
        Params(ip = ip, cameraName = cameraName)
    )

    override suspend fun doWork(params: Params) = withContext(Dispatchers.IO) {
        dataSource.updateCameraName(cameraName = params.cameraName).getOrThrow()
        val cfg = camManager.config ?: return@withContext

        Timber.d("cfg.cameraId = ${cfg.cameraId}")

        val cam = KuCameraPreference.find(cameraId = cfg.cameraId)
        if (cam != null) {
            KuCameraPreference.add(
                cam.copy(cameraName = params.cameraName)
            )
        }

        camManager.updateConfig {
            it.copy(cameraName = params.cameraName)
        }
    }
}
