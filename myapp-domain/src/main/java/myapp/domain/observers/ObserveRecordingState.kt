package myapp.domain.observers

import com.dropbox.android.external.store4.get
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import myapp.data.cam.CamManager
import myapp.data.entities.KuRecordingState
import myapp.data.repo.cam.RecordingStateStore
import myapp.data.repo.cam.api.CamDataSource
import myapp.util.Logger
import javax.inject.Inject

class ObserveRecordingState @Inject constructor(
    private val logger: Logger,
    private val dataSource: CamDataSource,
    private val camManager: CamManager,
    private val recordingStateStore: RecordingStateStore
) {

    fun observe(): Flow<KuRecordingState> {
        return flow {
            while (currentCoroutineContext().isActive) {
                val ip = camManager.cameraIp
                if (ip != null) {
                    try {
                        emit(refresh())
                    } catch (ignore: Throwable) {
                        logger.d(ignore)
                    }
                }
                delay(3000)
            }
        }
    }

    private suspend fun refresh(): KuRecordingState {
        logger.d("XXX recording state refresh")
        val state = recordingStateStore.store().get(key = "1")
        camManager.updateRecordingState(state)
        return state
    }
}
