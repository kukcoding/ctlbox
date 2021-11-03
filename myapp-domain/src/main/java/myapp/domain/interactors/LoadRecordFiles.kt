package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.entities.KuRecordFile
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.ResultInteractor
import myapp.util.Logger
import javax.inject.Inject

class LoadRecordFiles @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource
) : ResultInteractor<Unit, List<KuRecordFile>>() {

    override suspend fun doWork(params: Unit): List<KuRecordFile> = withContext(Dispatchers.IO) {
        val files = dataSource.recordFiles().getOrThrow()
        files.sortedByDescending { it.dateTime }
    }
}
