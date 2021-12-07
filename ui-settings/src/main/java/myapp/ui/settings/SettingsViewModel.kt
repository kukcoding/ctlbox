package myapp.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kr.ohlab.android.recyclerviewgroup.ItemBase
import myapp.ReduxViewModel
import myapp.api.UiError
import myapp.data.cam.CamManager
import myapp.data.cam.RecordingState
import myapp.data.cam.RecordingTracker
import myapp.domain.interactors.UpdateCameraTime
import myapp.domain.observers.ObserveRecordingState
import myapp.ui.SnackbarManager
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import javax.inject.Inject


internal sealed class SettingsAction {
    object Refresh : SettingsAction()
}


internal data class SettingsState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val error: UiError? = null,
    val cameraIp: String? = null
)


private fun formatRecordDate(startTime: Instant, durationMinute: Long): String {
    val from = startTime.atZone(ZoneId.systemDefault())
    val txt1 =
        "${from.year}년 ${from.monthValue}월 ${from.dayOfMonth}일 ${from.hour}시 ${from.minute}분 ${from.second}초"

    if (durationMinute <= 0) {
        return "${txt1}부터"
    }

    val to = from.plusMinutes(durationMinute)
    val txt2 = when {
        from.year != to.year -> "${to.year}년 ${to.monthValue}월 ${to.dayOfMonth}일 ${to.hour}시 ${to.minute}분 ${to.second}초"
        from.monthValue != to.monthValue -> "${to.monthValue}월 ${to.dayOfMonth}일 ${to.hour}시 ${to.minute}분 ${to.second}초"
        from.dayOfMonth != to.dayOfMonth -> "${to.dayOfMonth}일 ${to.hour}시 ${to.minute}분 ${to.second}초"
        from.hour != to.hour -> "${to.hour}시 ${to.minute}분 ${to.second}초"
        else -> "${to.minute}분 ${to.second}초"
    }

    return "${txt1}부터 ${txt2}까지"
}

private fun formatDateEpochSeconds(time: Long): String {
    val now = Instant.ofEpochSecond(time)
    val from = now.atZone(ZoneId.systemDefault())
    return "${from.year}년 ${from.monthValue}월 ${from.dayOfMonth}일 ${from.hour}시 ${from.minute}분 ${from.second}초"
}

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    val camManager: CamManager,
    private val snackbarManager: SnackbarManager,
    val recordingTracker: RecordingTracker,
    private val observeRecordingState: ObserveRecordingState,
    private val updateCameraTime: UpdateCameraTime,
    private val logger: Logger
) : ReduxViewModel<SettingsState>(SettingsState()) {
    private val loadingState = ObservableLoadingCounter()
    private val pendingActions = Channel<SettingsAction>(Channel.BUFFERED)

    val viewItems = MutableLiveData<List<ItemBase<*>>>()
    val isLoadingLive = liveFieldOf(SettingsState::isLoading)

    val streamingQualityTextLive = camManager.observeConfig().map { cfg ->
        if (cfg != null) {
            "${cfg.streaming.resolution} / ${cfg.streaming.fps} FPS"
        } else {
            "-"
        }
    }.asLiveData()

    val recordingQualityTextLive = camManager.observeConfig().map { cfg ->
        if (cfg != null) {
            "${cfg.recording.resolution} / ${cfg.recording.fps} FPS"
        } else {
            "-"
        }
    }.asLiveData()

    val cameraCurrentTimeTextLive = camManager.observeConfig().map { cfg ->
        if (cfg != null) {
            formatDateEpochSeconds(cfg.timeSeconds)
        } else {
            "-"
        }
    }.asLiveData()

    // val isRecordingLive = recordingTracker.isRecordingFlow.asLiveData()
//    val recordingStateTextLive = recordingTracker.stateFlow.map { state ->
//        when (state) {
//            is RecordingState.Disabled -> "중지됨"
//            is RecordingState.FiniteRecording -> "녹화중"
//            is RecordingState.InfiniteRecording -> "녹화중"
//            is RecordingState.RecordingScheduled -> "녹화 예약됨"
//            is RecordingState.RecordingExpired -> "녹화 종료"
//        }
//    }.asLiveData()
    val isRecordingLive = observeRecordingState.observe().map { it.running }.asLiveData()
    val recordingStateTextLive = observeRecordingState.observe().map { state ->
        when (state.running) {
            true -> "녹화중"
            else -> "녹화중지"
        }
    }.asLiveData()

    val recordingScheduleVisibleLive = recordingTracker.stateFlow.map { it !is RecordingState.Disabled }.asLiveData()

    val cameraNameLive = camManager.observeConfig().map { cfg ->
        cfg?.cameraName ?: "-"
    }.asLiveData()

    val enabledNetworkTextLive = camManager.observeConfig().map { cfg ->
        cfg?.enabledNetworkMedia?.uppercase() ?: "-"
    }.asLiveData()

    val wifiSsidLive = camManager.observeConfig().map { cfg ->
        cfg?.wifiSsid ?: "-"
    }.asLiveData()

    init {
        // 스낵바 에러 처리
        snackbarManager.launchInScope(viewModelScope) { uiError, visible ->
            viewModelScope.launchSetState {
                copy(error = if (visible) uiError else null)
            }
        }

        // 로딩 상태 관찰
        viewModelScope.launch {
            loadingState.observable.collectAndSetState { loading ->
                copy(isLoading = loading)
            }
        }


        // 카메라 IP 상태 관찰
        viewModelScope.launch {
            camManager.observeCameraIp().collectAndSetState { cameraIp ->
                copy(cameraIp = cameraIp)
            }
        }


        // 액션 처리
        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is SettingsAction.Refresh -> refresh(force = true)
                }
            }
        }
    }

    fun submitAction(action: SettingsAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                pendingActions.send(action)
            }
        }
    }

    private suspend fun refresh(force: Boolean) {
        loadingState.addLoader()
//        val result = syncSongGroups.executeSync(force = force)
//        if (result is ErrorResult) {
//            snackbarManager.sendError(UiError(result.throwable))
//        }
        loadingState.removeLoader()
    }

    suspend fun updateTime() {
        updateCameraTime.executeSync()
    }
}
