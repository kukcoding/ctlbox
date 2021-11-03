package myapp.data.entities.network

import androidx.annotation.Keep

@Keep
data class Resolutions(
    val recording: List<String>,
    val streaming: List<String>,
)
