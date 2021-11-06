package myapp.data.cam

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.*
import myapp.ActivityLauncher
import myapp.data.entities.KuCameraConfig
import myapp.util.tupleOf

sealed class CamLoginState
object CamLoggedOut : CamLoginState()
data class CamLoggedIn(val cameraId: String, val cameraIp: String) : CamLoginState()


class CamManager constructor(
    private val context: Context,
    private val activityLauncher: ActivityLauncher
) {
    /**
     * 로그인 상태 모니터링
     */
    val loginStateFlow = MutableStateFlow<CamLoginState>(CamLoggedOut)

    /**
     * 카메라 설정 정보 조회
     */
    private val configFlow = MutableStateFlow<KuCameraConfig?>(null)

    /**
     * 연결 끊김 메시지 알림의 상태
     * DISABLED 인경우 메시지 알림을 하지 않는다
     */
    private enum class MonitorState {
        DISABLED, ENABLED, DISCONNECTED
    }

    /**
     * 연결 끊김 메시지 관리 객체 - inner class
     * 연결된 상태에서 끊어지는 상황을 모니터링하기 위한 객체
     * 연결된 상태에서 연결이 끊어지면 연결이 끊어졌다는 메시지를 표시한다
     */
    val disconnectedMessage = DisconnectMessage()

    inner class DisconnectMessage {

        /**
         * 연결 끊김 메시지를 비활성하기 - flow
         * 값이 0보다 크면, 비활성 상태이다
         */
        private val disabledRequestCount = MutableStateFlow(0)

        fun addDisableRequest() {
            disabledRequestCount.value += 1
        }

        fun removeDisableRequest() {
            disabledRequestCount.value -= 1
        }

        fun setDisableRequest(cnt: Int) {
            disabledRequestCount.value = cnt
        }

        /**
         * 연결 끊김 메시지 표시하기
         */
        fun show(activity: Activity) {
            activityLauncher.startDisconnectedMessage(activity)
        }

        /**
         * 연결이 끊김 이벤트 발행 flow
         */
        val flow = combine(
            disabledRequestCount,
            configFlow,
            ::tupleOf
        ).map { (disabledRequest, config) ->
            if (disabledRequest > 0) {
                MonitorState.DISABLED
            } else {
                if (config != null) MonitorState.ENABLED else MonitorState.DISCONNECTED
            }
        }.distinctUntilChanged().flatMapLatest {
            when (it) {
                MonitorState.DISABLED -> emptyFlow()
                else -> flowOf(it == MonitorState.DISCONNECTED)
            }
        }
    }


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

    fun onLogout() {
        this.configFlow.tryEmit(null)
        this.loginStateFlow.tryEmit(CamLoggedOut)
    }

}

