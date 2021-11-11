package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.repo.cam.RecordingStateStore
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import javax.inject.Inject

class UpdateWifi @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource,
    private val camManager: CamManager,
    private val recordingStateStore: RecordingStateStore

) : Interactor<UpdateWifi.Params>() {

    data class Params(val ip: String, val wifiSsid: String, val wifiPw: String)

    suspend fun executeSync(ip: String, wifiSsid: String, wifiPw: String) = executeSync(
        Params(ip = ip, wifiSsid = wifiSsid, wifiPw = wifiPw)
    )

    override suspend fun doWork(params: Params) = withContext(Dispatchers.IO) {
        dataSource.updateWifi(
            wifiSsid = params.wifiSsid,
            wifiPw = params.wifiPw
        ).getOrThrow()

        recordingStateStore.store().clear("1")
        camManager.updateConfig { old ->
            old.copy(
                wifiSsid = params.wifiSsid,
                wifiPw = params.wifiPw
            )
        }
    }
}
