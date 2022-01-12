package myapp.domain.interactors

import com.dropbox.android.external.store4.fresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import myapp.BuildVars
import myapp.data.cam.CamManager
import myapp.data.entities.KuRecordingSchedule
import myapp.data.repo.cam.RecordingStateStore
import myapp.data.repo.cam.api.CamDataSource
import myapp.domain.Interactor
import myapp.util.Logger
import org.threeten.bp.Instant
import javax.inject.Inject

class SaveRecordingSchedule @Inject constructor(
    private val logger: Logger,
    private val camManager: CamManager,
    private val dataSource: CamDataSource,
    private val recordingStateStore: RecordingStateStore
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
        if (params.disabled) {
            dataSource.updateRecordingOff()
        } else {
            dataSource.updateRecordingSchedule(
                startTime = params.startTime,
                durationMinute = params.durationMinute
            )
        }.getOrThrow()
        delay(1500)
        val cfg = dataSource.config().getOrThrow()
        if (BuildVars.fakeCamera) {
            camManager.updateConfig { old ->
                old.copy(
                    recordingSchedule = if (params.disabled) {
                        KuRecordingSchedule.DISABLED
                    } else {
                        old.recordingSchedule.copy(
                            disabled = false,
                            startTimestamp = params.startTime,
                            durationMinute = params.durationMinute
                        )
                    }
                )
            }
        } else {
            camManager.updateConfig { cfg }
        }
        recordingStateStore.store().fresh("1")
        Unit
    }
}
