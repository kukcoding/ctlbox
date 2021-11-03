package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import javax.inject.Inject

class SaveNetworkMedia @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource,
    private val camManager: CamManager
) : Interactor<SaveNetworkMedia.Params>() {

    data class Params(val ip: String, val networkMedia: String)

    suspend fun executeSync(ip: String, networkMedia: String) = executeSync(
        Params(ip = ip, networkMedia = networkMedia)
    )

    override suspend fun doWork(params: Params) = withContext(Dispatchers.IO) {
        dataSource.updateNetworkConfig(
            lte = params.networkMedia.contains("lte"),
            wifi = params.networkMedia.contains("wifi"),
        ).getOrThrow()

        camManager.updateConfig { old ->
            old.copy(enabledNetworkMedia = params.networkMedia)
        }
    }
}
