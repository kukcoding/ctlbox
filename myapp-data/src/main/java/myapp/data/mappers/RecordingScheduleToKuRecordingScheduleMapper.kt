package myapp.data.mappers

import myapp.data.entities.KuRecordingSchedule
import myapp.data.entities.network.CamRecordingSchedulePayload
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordingScheduleToKuRecordingScheduleMapper @Inject constructor(
) : Mapper<CamRecordingSchedulePayload.Response, KuRecordingSchedule> {
    override suspend fun map(from: CamRecordingSchedulePayload.Response) = KuRecordingSchedule(
        disabled = from.disabled,
        startTimestamp = Instant.ofEpochSecond(from.startAt),
        durationMinute = from.duration
    )
}
