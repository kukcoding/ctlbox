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
        return callApi {
            camApi.health(ip = ip)
        }.map {
            it.cid
        }
    }


    suspend fun logout(): Result<Unit> {
        val result = execApi { camApi.logout(ip = cameraIp()) }
        ApiPreference.accessToken.value = ""
        ApiPreference.cameraIp.value = ""
        ApiPreference.cameraId.value = ""
        return result
    }

    suspend fun config(): Result<KuCameraConfig> {
        return callApi {
            camApi.config(ip = cameraIp())
        }.map(camConfigToKuCameraConfigMapper::map)
    }

    suspend fun recordFiles(): Result<List<KuRecordFile>> {
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


    suspend fun deleteFile(fileId: String): Result<Unit> {
        return callApi { camApi.deleteFile(ip = cameraIp(), fileId = fileId) }
    }

    suspend fun updateRecordingResolution(resolution: String): Result<Unit> {
        return callApi { camApi.updateRecordingResolution(ip = cameraIp(), resolution = resolution) }
    }

    suspend fun updateNetworkConfig(wifi: Boolean, lte: Boolean): Result<Unit> {
        val enabled = when {
            wifi && lte -> "wifi,lte"
            lte -> "lte"
            wifi -> "wifi"
            else -> "off"
        }
        return callApi { camApi.updateNetworkConfig(ip = cameraIp(), enabled = enabled) }
    }

    suspend fun updatePassword(newPasswd: String): Result<Unit> {
        return callApi { camApi.updatePassword(ip = cameraIp(), pw = newPasswd) }
    }

    suspend fun updateCameraName(cameraName: String): Result<Unit> {
        return callApi { camApi.updateCameraName(ip = cameraIp(), cameraName = cameraName) }
    }
}
