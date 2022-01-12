package myapp.ui.home

import android.content.Context
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import myapp.ReduxViewModel
import myapp.data.cam.*
import myapp.data.code.CamConnectivity
import myapp.data.entities.KuCameraConfig
import myapp.domain.interactors.LogoutCamera
import myapp.domain.interactors.SaveRecordingSchedule
import myapp.domain.observers.ObserveRecordingState
import org.threeten.bp.Instant
import timber.log.Timber
import javax.inject.Inject

internal sealed class HomeAction {
    object Logout : HomeAction()
}


internal data class HomeState(
    val camConnectivity: CamConnectivity = CamConnectivity.DISABLED,
    val loginState: CamLoginState = CamLoggedOut,
    val camConfig: KuCameraConfig? = null,
    val logoutRunning: Boolean = false,
)


@HiltViewModel
internal class HomeViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logoutCamera: LogoutCamera,
    val camManager: CamManager,
    private val recordingTracker: RecordingTracker,
    private val observeRecordingState: ObserveRecordingState,
    private val saveRecordingSchedule: SaveRecordingSchedule,
) : ReduxViewModel<HomeState>(HomeState()) {
    private val pendingActions = MutableSharedFlow<HomeAction>()

    val enabledNetworkMediaTextLive = liveFieldOf(HomeState::camConfig).map {
        it?.enabledNetworkMedia?.uppercase()
    }

    val isLoggedInLive = liveFieldOf(HomeState::loginState).map { it is CamLoggedIn }
    val isLogoutRunningLive = liveFieldOf(HomeState::logoutRunning)

    val camStateTextLive = isLoggedInLive.map { yes ->
        if (yes) {
            "카메라에 연결되었습니다"
        } else {
            "카메라에 로그인해주세요"
        }
    }

    val loginButtonTextLive = isLoggedInLive.map { yes ->
        if (yes) {
            "로그아웃"
        } else {
            "로그인"
        }
    }

    val cameraNameLive = liveFieldOf(HomeState::camConfig).map {
        it?.cameraName ?: "CAMERA"
    }

    val rtspResolutionTextLive = liveFieldOf(HomeState::camConfig).map {
        it?.streaming?.resolution ?: ""
    }

    val mjpgResolutionTextLive = liveFieldOf(HomeState::camConfig).map {
        it?.mjpg?.resolution ?: "1280x720"
    }

    val isWifiEnabledLive = liveFieldOf(HomeState::camConfig).map { it?.enabledNetworkMedia?.contains("wifi") }
    val isLteEnabledLive = liveFieldOf(HomeState::camConfig).map { it?.enabledNetworkMedia?.contains("lte") }
    val isWifiAvailableLive = liveFieldOf(HomeState::camConfig).map { it?.availableNetworkMedia?.contains("wifi") }
    val isLteAvailableLive = liveFieldOf(HomeState::camConfig).map { it?.availableNetworkMedia?.contains("lte") }

    val camIpTextLive = liveFieldOf(HomeState::loginState).map {
        if (it is CamLoggedIn) {
            it.cameraIp
        } else {
            null
        }
    }

    val camIdTextLive = liveFieldOf(HomeState::loginState).map {
        if (it is CamLoggedIn) {
            it.cameraId
        } else {
            null
        }
    }

//    val recordingStateTextLive = recordingTracker.stateFlow.map { state ->
//        when (state) {
//            is RecordingState.Disabled -> "녹화중지"
//            is RecordingState.FiniteRecording -> "녹화중"
//            is RecordingState.InfiniteRecording -> "녹화중"
//            is RecordingState.RecordingScheduled -> "녹화 예약됨"
//            is RecordingState.RecordingExpired -> "녹화 종료"
//        }
//    }.asLiveData()

    val recordingStateTextLive = observeRecordingState.observe().map { state ->
        when (state.running) {
            true -> "녹화중"
            else -> "녹화중지"
        }
    }.asLiveData()

    val isRecordingLive = recordingTracker.isRecordingFlow.asLiveData()

    init {
        // 액션 처리
        viewModelScope.launch {
            pendingActions.collect { action ->
                when (action) {
                    is HomeAction.Logout -> doLogout()
                }
            }
        }

        viewModelScope.launch {
            camManager.loginStateFlow.collect { setState { copy(loginState = it) } }
        }

        viewModelScope.launch {
            camManager.observeConfig().collect { setState { copy(camConfig = it) } }
        }
    }


    fun submitAction(action: HomeAction) {
        viewModelScope.launch {
            pendingActions.emit(action)
        }
    }

    private suspend fun doLogout() {
        setState { copy(logoutRunning = true) }
        try {
            logoutCamera.executeSync(Unit)
        } catch (err: Throwable) {
            Timber.d("err=" + err.message)
        } finally {
            setState { copy(logoutRunning = false) }
        }
    }

    suspend fun startRecordNow(ip: String) {
        saveRecordingSchedule.executeSync(
            ip = ip,
            disabled = false,
            startTime = Instant.ofEpochSecond(0),
            durationMinute = -1L
        )
    }

    suspend fun stopRecord(ip: String) {
        saveRecordingSchedule.executeSync(
            ip = ip,
            disabled = true,
            startTime = Instant.ofEpochSecond(0),
            durationMinute = -1L
        )
    }
}
