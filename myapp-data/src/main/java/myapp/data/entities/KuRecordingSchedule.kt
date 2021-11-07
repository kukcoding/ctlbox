package myapp.data.entities

import org.threeten.bp.Instant

data class KuRecordingSchedule(
    val disabled: Boolean,

    val startTimestamp: Instant?,

    // -1 상시녹확
    val durationMinute: Long
) {
    companion object {
        val DISABLED = KuRecordingSchedule(disabled = false, startTimestamp = null, durationMinute = -1L)
    }
}
