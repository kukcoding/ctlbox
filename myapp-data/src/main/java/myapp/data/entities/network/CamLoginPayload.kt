package myapp.data.entities.network

import androidx.annotation.Keep
import myapp.data.entities.MjpgQuality
import myapp.data.entities.VideoQuality


class CamLoginPayload {
    @Keep
    data class Login(
        val sid: String,
        val cameraId: String,
        val cameraName: String,
        val resolutions: Resolutions,
        val network: Network,
        val recording: VideoQuality,
        val streaming: VideoQuality,
        val preview: MjpgQuality,
        val recordingSchedule: RecordingSchedule
    )
}

