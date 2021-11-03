package myapp.data.entities.network

import androidx.annotation.Keep


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
    )
}

