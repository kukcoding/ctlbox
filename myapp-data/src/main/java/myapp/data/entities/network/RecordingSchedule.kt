package myapp.data.entities.network

data class RecordingSchedule(
    val disabled: Boolean,
    val startAt: Long, // epoch
    val duration: Long, // duration in seconds
    val switchOn: Boolean
)
