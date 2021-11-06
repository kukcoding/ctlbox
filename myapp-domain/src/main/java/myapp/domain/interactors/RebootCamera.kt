package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import javax.inject.Inject

class RebootCamera @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource,
    private val camManager: CamManager
) : Interactor<RebootCamera.Params>() {

    data class Params(val ip: String)

    suspend fun executeSync(ip: String) = executeSync(Params(ip = ip))

    override suspend fun doWork(params: Params) = withContext(Dispatchers.IO) {
        try {
            dataSource.reboot().getOrThrow()
        } catch (throwable: Throwable) {
            logger.d("reboot error: ${throwable.message}")
        } finally {
            camManager.onLogout()
        }
    }
}
