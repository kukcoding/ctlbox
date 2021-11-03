package myapp.data.entities.network

import androidx.annotation.Keep

@Keep
data class MjpgQuality(
    val resolution: String,
    val fps: Int,
)
