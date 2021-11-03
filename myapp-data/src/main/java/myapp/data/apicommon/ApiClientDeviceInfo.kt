package myapp.data.apicommon

import androidx.annotation.Keep

@Keep
data class ApiClientDeviceInfo(
    /**
     * 기종 ID
     */
    val uuid: String,
    /**
     * 앱 버전
     */
    val appVersion: String,
    /**
     * OS 버전
     */
    val osVersion: Int, // apiLevel
    /**
     * 인증서 해시
     */
    val certHash: String,

    /**
     * 인증서 해시
     */
    val modelName: String
)
