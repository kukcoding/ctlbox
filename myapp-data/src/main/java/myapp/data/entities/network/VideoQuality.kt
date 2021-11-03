package myapp.data.entities.network

import androidx.annotation.Keep

@Keep
data class VideoQuality(
    val resolution: String,
    val fps: Int,
    val kbps: Int, // bitrate
)
