package myapp.domain.interactors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.data.cam.CamManager
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import org.threeten.bp.Instant
import javax.inject.Inject

class SaveRecordingSchedule @Inject constructor(
    private val logger: Logger,
    private val camManager: CamManager,
    private val dataSource: CamDataSource
) : Interactor<SaveRecordingSchedule.Params>() {

    data class Params(val ip: String, val disabled: Boolean, val startTime: Instant, val durationMinute: Long)

    suspend fun executeSync(ip: String, disabled: Boolean, startTime: Instant, durationMinute: Long) = executeSync(
        Params(
            ip = ip,
            disabled = disabled,
            startTime = startTime,
            durationMinute = durationMinute
        )
    )

    override suspend fun doWork(params: Params) = withContext(Dispatchers.IO) {
        val schedule = if (params.disabled) {
            dataSource.updateRecordingOff()
        } else {
            dataSource.updateRecordingSchedule(
                startTime = params.startTime,
                durationMinute = params.durationMinute
            )
        }.getOrThrow()

        camManager.updateConfig {
            it.copy(recordingSchedule = schedule)
        }
    }
}
