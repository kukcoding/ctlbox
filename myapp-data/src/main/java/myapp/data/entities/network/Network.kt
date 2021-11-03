package myapp.data.entities.network

import androidx.annotation.Keep

@Keep
data class Network(
    val enabled: List<String>,
    val available: List<String>,
)
