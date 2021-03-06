package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.entities.KuCamera
import myapp.data.entities.KuCameraConfig
import myapp.data.preferences.KuCameraPreference
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.ResultInteractor
import myapp.util.Logger
import org.threeten.bp.Instant
import javax.inject.Inject

class DoLogin @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource,
    private val camManager: CamManager
) : ResultInteractor<DoLogin.Params, KuCameraConfig>() {

    data class Params(val ip: String, val pw: String)

    suspend fun executeSync(ip: String, pw: String) = executeSync(
        Params(ip = ip, pw = pw)
    )

    override suspend fun doWork(params: Params): KuCameraConfig = withContext(Dispatchers.IO) {
        val cfg = dataSource.login(ip = params.ip, pw = params.pw).getOrThrow()
        val nowEpochSecond = Instant.now().epochSecond
        dataSource.updateTime(timeSeconds = nowEpochSecond)
        camManager.updateConfig(cfg)
        camManager.onConnected(cameraId = cfg.cameraId, cameraIp = params.ip)
        KuCameraPreference.add(
            KuCamera(
                cameraId = cfg.cameraId,
                cameraName = cfg.cameraName,
                lastConnectTimestamp = System.currentTimeMillis(),
                lastIp = params.ip
            )
        )
        cfg
    }
}
