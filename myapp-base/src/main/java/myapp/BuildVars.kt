package myapp

import android.net.Uri
import kotlinx.coroutines.CompletableDeferred
import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

object BuildVars {
    const val cameraAccessPointIp = "192.168.157.100"
    const val fakeCamera = true
    const val fakeCameraId = "fakeab0012abde"

    /**
     * 카메라 재부팅 시간
     */
    const val rebootDurationSec = 20


    /**
     * WIFI SSID 길이
     */
    const val wifiSsidMinLength = 4
    const val wifiSsidMaxLength = 30


    /**
     * WIFI PW 길이
     */
    const val wifiPwMinLength = 8
    const val wifiPwMaxLength = 30


    /**
     * 카메라 PW 길이
     */
    const val cameraPwMinLength = 4
    const val cameraPwMaxLength = 30

    /**
     * 카메라 이름
     */
    const val cameraNameMinLength = 2
    const val cameraNameMaxLength = 30
}


object Cam {
    fun url(ip: String, path: String): String {
        return if (path.startsWith("/")) {
            "http://${ip}${path}"
        } else {
            "http://${ip}/${path}"
        }
    }

    fun rtspUrl(ip: String): String {
        // Uri.parse("rtsp://192.168.114.60:8554/live")
        return "rtsp://${ip}:554/live"
    }

    fun uri(ip: String, path: String): Uri {
        return Uri.parse(url(ip, path))
    }


    fun recordFileUrl(ip: String, fileId: String): String {
        return url(ip = ip, path = "/recording/download?fileName=${fileId}")
    }

    fun thumbnailUrl(ip: String, fileId: String, timestamp: Long): String {
        return url(ip = ip, "/recording/thumb?fileName=${fileId}&_t=${timestamp}")
    }

    private fun healthUrl(ip: String): String {
        return url(ip = ip, path = "/")
    }

    // TODO 제거 예정
    suspend fun checkConnectable(ip: String, timeoutSeconds: Long = 10): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        val client = OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .build()
        try {
            val request = Request.Builder().url(healthUrl(ip = ip)).build()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    deferred.complete(false)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        response.close()
                    } catch (ignore: Throwable) {
                    }
                    deferred.complete(true)
                }
            })

        } catch (ignore: IOException) {
            deferred.complete(false)
        }
        return deferred.await()
    }
}

