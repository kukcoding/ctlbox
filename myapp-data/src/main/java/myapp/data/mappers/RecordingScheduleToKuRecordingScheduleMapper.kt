package myapp.data.mappers

import myapp.data.entities.KuRecordingSchedule
import myapp.data.entities.network.CamRecordingSchedulePayload
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

@Singleton
class RecordingScheduleToKuRecordingScheduleMapper @Inject constructor(
) : Mapper<CamRecordingSchedulePayload.Response, KuRecordingSchedule> {
    override suspend fun map(from: CamRecordingSchedulePayload.Response) = KuRecordingSchedule(
        disabled = !from.enabled,
        startTimestamp = Instant.ofEpochSecond(from.startAt),
        durationMinute = if (from.duration <= 0) 0L else (from.duration / 60f).roundToLong(),
        switchOn = from.switchOn
    )
}
