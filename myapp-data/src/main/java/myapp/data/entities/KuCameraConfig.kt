package myapp.data.entities

data class VideoQuality(
    val resolution: String, //  1920x1080
    val fps: Int, // 15
    val kbps: Int, // bitrate
)

data class MjpgQuality(
    val resolution: String, //  1920x1080
    val fps: Int, // 15
)

data class KuCameraConfig(
    val cameraId: String,
    var cameraName: String?,
    var recordingResolutions: List<String>, // listOf(3840x2160,1920x1080,1280x720)
    var streamingResolutions: List<String>, // listOf(1920x1080,1280x720,640x480)
    var enabledNetworkMedia: String, // wifi,lte
    var availableNetworkMedia: String, // wifi,lte
    //var networkAvailableList: List<String>, // [wifi, lte]
    var recording: VideoQuality,
    var streaming: VideoQuality,
    var mjpg: MjpgQuality ,
)
