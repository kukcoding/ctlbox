package myapp.data.repo.cam.api

import myapp.BuildVars
import myapp.data.apicommon.callApi
import myapp.data.apicommon.execApi
import myapp.data.entities.*
import myapp.data.mappers.CamConfigToKuCameraConfigMapper
import myapp.data.mappers.LoginToKuCameraConfigMapper
import myapp.data.mappers.RecordFileIdToKuRecordFileMapper
import myapp.data.preferences.ApiPreference
import javax.inject.Inject


private fun createFakeConfig(): KuCameraConfig {
    return KuCameraConfig(
        cameraId = BuildVars.fakeCameraId,
        cameraName = "FAKE 카메라",
        recordingResolutions = listOf("1920x1080", "1280x720", "640x480"),
        streamingResolutions = listOf("1920x1080", "1280x720", "640x480"),
        enabledNetworkMedia = "wifi,lte",
        availableNetworkMedia = "wifi,lte",
        recording = VideoQuality(resolution = "1920x1080", fps = 25, kbps = 6400),
        streaming = VideoQuality(resolution = "1280x720", fps = 15, kbps = 3200),
        mjpg = MjpgQuality(resolution = "1280x720", fps = 10),
        wifiSsid = "mpx-cam-demo",
        wifiPw = "1111",
        recordStartTimestamp = null,
        recordDurationMinute = 0L,
    )
}

class CamDataSource @Inject constructor(
    private val camApi: CamApi,
    private val loginToKuCameraConfigMapper: LoginToKuCameraConfigMapper,
    private val camConfigToKuCameraConfigMapper: CamConfigToKuCameraConfigMapper,
    private val recordFileIdToKuRecordFileMapper: RecordFileIdToKuRecordFileMapper,
) {

    private fun cameraIp(): String {
        val ip = ApiPreference.cameraIp.value
        if (ip.isBlank()) {
            return BuildVars.cameraAccessPointIp
        }
        return ip
    }


    suspend fun login(ip: String, pw: String): Result<KuCameraConfig> {
        if (BuildVars.fakeCamera) {
            ApiPreference.accessToken.value = "1234"
            ApiPreference.cameraIp.value = ip
            ApiPreference.cameraId.value = BuildVars.fakeCameraId
            return Success(createFakeConfig())
        }

        val result = callApi { camApi.login(ip = ip, pw = pw) }.onSuccess { data ->
            ApiPreference.accessToken.value = data.sid
            ApiPreference.cameraIp.value = ip
            ApiPreference.cameraId.value = data.cameraId
        }.map(loginToKuCameraConfigMapper::map)

        return result
    }


    /**
     * 카메라 상태 체크 - 해당 IP 주소가 카메라인지 체크한다
     *
     */
    suspend fun health(ip: String): Result<String> {
        if (BuildVars.fakeCamera) {
            return Success(BuildVars.fakeCameraId)
        }

        return callApi {
            camApi.health(ip = ip)
        }.map {
            it.cid
        }
    }



    /**
     * 카메라 설정 정보 조회
     */
    suspend fun config(): Result<KuCameraConfig> {
        if (BuildVars.fakeCamera) {
            return Success(createFakeConfig())
        }
        return callApi {
            camApi.config(ip = cameraIp())
        }.map(camConfigToKuCameraConfigMapper::map)
    }

    /**
     * 녹화 파일 목록 조회
     */
    suspend fun recordFiles(): Result<List<KuRecordFile>> {
        if (BuildVars.fakeCamera) {
            // ${yymmdd}_${hhmmss}_${width}x${height}_${fps}_${kbps}_${duration_msec}_${file_size}.mp4
            return Success(
                listOf(
                    "20211012_180556_3840x2160_15_1500_33327_7562350.mp4",
                    "20211012_180630_3840x2160_15_1500_33613_7548455.mp4",
                    "20211012_180703_3840x2160_15_1500_33372_7558826.mp4",
                    // <-- means that file is recording now (not closed)
                    "20211012_180737_3840x2160_15_1500_0_0.mp4",
                ).map {
                    recordFileIdToKuRecordFileMapper.map(it)
                }
            )
        }

        return callApi {
            camApi.recordFiles(ip = cameraIp())
        }.map { data ->
            val recordFiles = mutableListOf<KuRecordFile>()
            data.files.forEach { fileId ->
                recordFiles.add(recordFileIdToKuRecordFileMapper.map(fileId))
            }
            recordFiles
        }
    }

    /**
     * 녹화 파일 삭제
     */
    suspend fun deleteFile(fileId: String): Result<Unit> {
        if (BuildVars.fakeCamera) {
            return Success(Unit)
        }

        return callApi { camApi.deleteFile(ip = cameraIp(), fileId = fileId) }
    }


    /**
     * 녹화 품질 설정 업데이트
     */
    suspend fun updateRecordingVideoQuality(resolution: String, fps: Int): Result<Unit> {
        if (BuildVars.fakeCamera) {
            return Success(Unit)
        }

        return callApi { camApi.updateRecordingVideoQuality(ip = cameraIp(), resolution = resolution, fps = fps) }
    }

    /**
     * 스트리밍 품질 설정 업데이트
     */
    suspend fun updateStreamingVideoQuality(resolution: String, fps: Int): Result<Unit> {
        if (BuildVars.fakeCamera) {
            return Success(Unit)
        }

        return callApi { camApi.updateStreamingVideoQuality(ip = cameraIp(), resolution = resolution, fps = fps) }
    }

    /**
     * 카메라 네트워크 변경
     */
    suspend fun updateNetworkConfig(wifi: Boolean, lte: Boolean): Result<Unit> {
        if (BuildVars.fakeCamera) {
            return Success(Unit)
        }

        val enabled = when {
            wifi && lte -> "wifi,lte"
            lte -> "lte"
            wifi -> "wifi"
            else -> "off"
        }
        return callApi { camApi.updateNetworkConfig(ip = cameraIp(), enabled = enabled) }
    }

    /**
     * 카메라 비번 변경
     */
    suspend fun updatePassword(newPasswd: String): Result<Unit> {
        if (BuildVars.fakeCamera) {
            return Success(Unit)
        }

        return callApi { camApi.updatePassword(ip = cameraIp(), pw = newPasswd) }
    }

    /**
     * 카메라 이름 변경
     */
    suspend fun updateCameraName(cameraName: String): Result<Unit> {
        if (BuildVars.fakeCamera) {
            return Success(Unit)
        }

        return callApi { camApi.updateCameraName(ip = cameraIp(), cameraName = cameraName) }
    }

    /**
     * 와이파이 업데이트
     */
    suspend fun updateWifi(wifiSsid: String, wifiPw: String): Result<Unit> {
        if (BuildVars.fakeCamera) {
            return Success(Unit)
        }

        return callApi { camApi.updateWifi(ip = cameraIp(), ssid = wifiSsid, pw = wifiPw) }
    }


    suspend fun logout(): Result<Unit> {
        if (BuildVars.fakeCamera) {
            ApiPreference.accessToken.value = ""
            ApiPreference.cameraIp.value = ""
            ApiPreference.cameraId.value = ""
            return Success(Unit)
        }

        val result = execApi { camApi.logout(ip = cameraIp()) }
        ApiPreference.accessToken.value = ""
        ApiPreference.cameraIp.value = ""
        ApiPreference.cameraId.value = ""
        return result
    }


    /**
     * 재부팅
     */
    suspend fun reboot(): Result<Unit> {
        if (BuildVars.fakeCamera) {
            ApiPreference.accessToken.value = ""
            ApiPreference.cameraIp.value = ""
            ApiPreference.cameraId.value = ""
            return Success(Unit)
        }

        return callApi { camApi.reboot(ip = cameraIp()) }
    }
}
