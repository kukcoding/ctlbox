package myapp.data.mappers

import myapp.data.entities.KuCameraConfig
import myapp.data.entities.MjpgQuality
import myapp.data.entities.VideoQuality
import myapp.data.entities.network.CamLoginPayload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginToKuCameraConfigMapper @Inject constructor(
) : Mapper<CamLoginPayload.Login, KuCameraConfig> {
    override suspend fun map(from: CamLoginPayload.Login) = KuCameraConfig(
        cameraId = from.cameraId,
        cameraName = from.cameraName,
        recordingResolutions = from.resolutions.recording,
        streamingResolutions = from.resolutions.streaming,
        recording = VideoQuality(
            resolution = from.recording.resolution,
            fps = from.recording.fps,
            kbps = from.recording.kbps,
        ),
        streaming = VideoQuality(
            resolution = from.streaming.resolution,
            fps = from.streaming.fps,
            kbps = from.streaming.kbps,
        ),
        enabledNetworkMedia = from.network.enabled.joinToString(","),
        availableNetworkMedia = from.network.available.joinToString(","),
        mjpg = MjpgQuality(resolution = from.preview.resolution, fps = from.preview.fps),
        wifiSsid = null,
        wifiPw = null,
        recordStartTimestamp = null,
        recordDurationMinute = 0L,
    )
}
