package myapp.data.entities

data class KuCameraConfig(
    val cameraId: String,
    val cameraName: String?,
    val recordingResolutions: List<String>, // listOf(3840x2160,1920x1080,1280x720)
    val streamingResolutions: List<String>, // listOf(1920x1080,1280x720,640x480)
    val enabledNetworkMedia: String, // wifi,lte
    val availableNetworkMedia: String, // wifi,lte
    val recording: VideoQuality,
    val streaming: VideoQuality,
    val mjpg: MjpgQuality,
    val wifiSsid: String?,
    val wifiPw: String?,
    val recordingSchedule: KuRecordingSchedule
)
