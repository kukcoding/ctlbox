package myapp.data.entities.network

import androidx.annotation.Keep
import myapp.data.entities.MjpgQuality
import myapp.data.entities.VideoQuality

class CamConfigPayload {
    @Keep
    data class Response(
        val cameraId: String,
        val time: Long,
        val cameraName: String,
        val resolutions: Resolutions,
        val network: Network,
        val recording: VideoQuality,
        val streaming: VideoQuality,
        val preview: MjpgQuality,
        val recordingSchedule: RecordingSchedule
    )
}
