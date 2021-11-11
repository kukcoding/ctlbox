package myapp.data.entities.network

import androidx.annotation.Keep

class CamRecordingSchedulePayload {
    @Keep
    data class Response(
        val enabled: Boolean,
        val startAt: Long, // 초단위
        val duration: Long, // 초단위
        val switchOn: Boolean,
    )
}


class CamRecordingStatePayload {
    @Keep
    data class Response(
        val enabled: Boolean,
        val running: Boolean,
        val switchOn: Boolean,
    )
}
