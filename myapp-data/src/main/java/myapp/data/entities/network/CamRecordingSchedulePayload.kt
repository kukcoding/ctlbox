package myapp.data.entities.network

import androidx.annotation.Keep

class CamRecordingSchedulePayload {
    @Keep
    data class Response(
        val disabled: Boolean,
        val startAt: Long, // unic
        val duration: Long,
    )
}
