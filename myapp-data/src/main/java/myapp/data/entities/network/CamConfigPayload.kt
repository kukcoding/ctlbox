package myapp.data.entities.network

import androidx.annotation.Keep

class CamConfigPayload {
    @Keep
    data class Response(
        val cameraId: String,
        val cameraName: String,
        val resolutions: Resolutions,
        val network: Network,
        val recording: VideoQuality,
        val streaming: VideoQuality,
        val preview: MjpgQuality,
    )
}
