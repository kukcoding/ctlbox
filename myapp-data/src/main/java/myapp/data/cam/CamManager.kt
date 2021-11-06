package myapp.data.cam

import android.content.Context
import kotlinx.coroutines.flow.*
import myapp.data.entities.KuCameraConfig
import myapp.util.tupleOf

sealed class CamLoginState
object CamLoggedOut : CamLoginState()
data class CamLoggedIn(val cameraId: String, val cameraIp: String) : CamLoginState()


class CamManager constructor(
    private val context: Context
) {
    /**
     * 로그인 상태 모니터링
     */
    val loginStateFlow = MutableStateFlow<CamLoginState>(CamLoggedOut)

    /**
     * 카메라 설정 정보 조회
     */
    private val configFlow = MutableStateFlow<KuCameraConfig?>(null)

    val isLoggedIn: Boolean
        get() {
            return this.loginStateFlow.value is CamLoggedIn
        }

    val config: KuCameraConfig?
        get() {
            if (!isLoggedIn) return null
            return this.configFlow.value
        }

    val cameraIp: String?
        get() {
            val state = this.loginStateFlow.value
            return if (state is CamLoggedIn) state.cameraIp else null
        }

    val cameraId: String?
        get() {
            val state = this.loginStateFlow.value
            return if (state is CamLoggedIn) state.cameraId else null
        }

    suspend fun ensureConfigExists(): KuCameraConfig {
        return this.observeConfig().filterNotNull().first()
    }

    /**
     * 로그인 상태와 카메라 설정 Flow
     * 로그인 상태에서만 설정값이 존재한다
     */
    fun observeConfig(): Flow<KuCameraConfig?> {
        return combine(loginStateFlow, configFlow, ::tupleOf).map { (state, cfg) ->
            if (state is CamLoggedIn) cfg else null
        }
    }

    fun observeCameraIp(): Flow<String?> {
        return loginStateFlow.map { state ->
            if (state is CamLoggedIn) state.cameraIp else null
        }
    }

    fun observeCameraId(): Flow<String?> {
        return loginStateFlow.map { state ->
            if (state is CamLoggedIn) state.cameraId else null
        }
    }

    fun updateConfig(action: (config: KuCameraConfig) -> KuCameraConfig) {
        val cfg = this.config
        if (cfg != null) {
            configFlow.tryEmit(action(cfg))
        }
    }

    fun updateConfig(config: KuCameraConfig) {
        configFlow.tryEmit(config)
    }

    fun onConnected(cameraId: String, cameraIp: String) {
        this.loginStateFlow.tryEmit(CamLoggedIn(cameraId = cameraId, cameraIp = cameraIp))
    }

    fun onDisconnect() {
        this.configFlow.tryEmit(null)
        this.loginStateFlow.tryEmit(CamLoggedOut)
    }

}

