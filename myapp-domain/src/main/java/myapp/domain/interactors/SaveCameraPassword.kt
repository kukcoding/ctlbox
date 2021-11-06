package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import javax.inject.Inject

class SaveCameraPassword @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource
) : Interactor<SaveCameraPassword.Params>() {

    data class Params(val ip: String, val pw: String)

    suspend fun executeSync(ip: String, pw: String) = executeSync(
        Params(ip = ip, pw = pw)
    )

    override suspend fun doWork(params: Params) = withContext(Dispatchers.IO) {
        dataSource.updatePassword(pw = params.pw).getOrThrow()
    }
}
