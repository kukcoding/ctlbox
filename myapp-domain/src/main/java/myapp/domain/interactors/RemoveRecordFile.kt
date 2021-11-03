package myapp.domain.interactors

import myapp.data.entities.Result
import myapp.data.entities.Success
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.ResultInteractor
import myapp.util.Logger
import javax.inject.Inject

class RemoveRecordFile @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource
) : ResultInteractor<RemoveRecordFile.Params, Result<Unit>>() {

    data class Params(val fileId: String)

    suspend fun executeSync(fileId: String) = executeSync(Params(fileId = fileId))

    override suspend fun doWork(params: Params): Result<Unit> {
        dataSource.deleteFile(fileId = params.fileId)
        return Success(Unit)
    }

}
