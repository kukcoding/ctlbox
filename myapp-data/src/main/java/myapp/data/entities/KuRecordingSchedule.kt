package myapp.data.entities

import org.threeten.bp.Instant

data class KuRecordingSchedule(
    val disabled: Boolean,

    val startTimestamp: Instant?,

    // -1 μμλΉν
    val durationMinute: Long,

    val switchOn: Boolean
) {
    companion object {
        val DISABLED = KuRecordingSchedule(disabled = false, startTimestamp = null, durationMinute = -1L, switchOn = false )
    }
}
