package myapp.data.mappers

import myapp.data.entities.KuRecordingState
import myapp.data.entities.network.CamRecordingStatePayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingStateToKuRecordingStateMapper @Inject constructor(
) : Mapper<CamRecordingStatePayload.Response, KuRecordingState> {
    override suspend fun map(from: CamRecordingStatePayload.Response) = KuRecordingState(
        disabled = !from.enabled,
        running = from.running,
        switchOn = from.switchOn
    )
}
