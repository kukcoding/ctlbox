package myapp.data.entities.network

import androidx.annotation.Keep

class CamHealthPayload {
    @Keep
    data class Response(val cid: String)
}
