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
import myapp.ui.SnackbarManager
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal sealed class SettingsAction {
    object Refresh : SettingsAction()
    object ToggleEditing : SettingsAction()
}


internal data class SettingsState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val error: UiError? = null,
    val cameraIp: String? = null
)

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    val camManager: CamManager,
    private val snackbarManager: SnackbarManager,
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

    val cameraNameLive = camManager.observeConfig().map { cfg ->
        cfg?.cameraName ?: "-"
    }.asLiveData()

    val enabledNetworkTextLive = camManager.observeConfig().map { cfg ->
        cfg?.enabledNetworkMedia?.uppercase() ?: "-"
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
                    is SettingsAction.ToggleEditing -> setState { copy(isEditing = !isEditing) }
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
}
