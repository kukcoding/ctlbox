package myapp.ui.dialogs.streamquality

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.data.code.VideoQualityKind
import myapp.domain.interactors.SaveRecordingQuality
import myapp.domain.interactors.SaveStreamingQuality
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal sealed class StreamQualityAction {
    data class Setup(
        val videoQualityKind: VideoQualityKind,
        val fps: Int,
        val resolution: String,
        val availableResolutions: List<String>
    ) : StreamQualityAction()

    data class SetResolution(val resolution: String) : StreamQualityAction()
    data class SetFps(val fps: Int) : StreamQualityAction()
}

internal data class StreamQualityViewState(
    val videoQualityKind: VideoQualityKind = VideoQualityKind.STREAMING,
    val availableResolutions: List<String> = emptyList(),
    val resolution: String = "1280x720",
    val fps: Int = 15,
)


@HiltViewModel
internal class StreamQualityDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val camManager: CamManager,
    private val saveStreamingQuality: SaveStreamingQuality,
    private val saveRecordingQuality: SaveRecordingQuality,
) : ReduxViewModel<StreamQualityViewState>(
    StreamQualityViewState()
) {
    private val pendingActions = Channel<StreamQualityAction>(Channel.BUFFERED)
    private val loadingState = ObservableLoadingCounter()

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()


    val fpsLive: LiveData<Int> = liveFieldOf(StreamQualityViewState::fps)
    val resolutionLive: LiveData<String> = liveFieldOf(StreamQualityViewState::resolution)

    fun isVisibleResolution(resolution: String): Boolean {
        return this.currentState().availableResolutions.contains(resolution)
    }

    init {
        viewModelScope.launch {
//            camManager.observeConfig().collect { cfg ->
//                Timber.w("camManager.observeConfig() cfg = ${cfg}")
//                if (cfg == null) {
//                    setState { copy(availableResolutions = emptyList()) }
//                } else {
//                    setState {
//                        copy(
//                            availableResolutions = cfg.recordingResolutions,
//                            resolution = cfg.recording.resolution,
//                            fps = cfg.recording.fps
//                        )
//                    }
//                }
//            }
        }

        // 액션 처리
        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is StreamQualityAction.SetResolution -> setState { copy(resolution = action.resolution) }
                    is StreamQualityAction.Setup -> setState {
                        copy(
                            videoQualityKind = action.videoQualityKind,
                            resolution = action.resolution,
                            availableResolutions = action.availableResolutions,
                            fps = action.fps
                        )
                    }
                    is StreamQualityAction.SetFps -> setState {
                        copy(fps = action.fps)
                    }
                }
            }
        }
    }

    fun updateFps(fps: Int) {
        viewModelScope.launch {
            setState { copy(fps = fps) }
        }
    }


    fun submitAction(vararg actions: StreamQualityAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                actions.forEach { pendingActions.send(it) }
            }
        }
    }


    suspend fun saveVideoQuality(ip: String, resolution: String, fps: Int) {
        val kind = this.currentState().videoQualityKind
        loadingState.addLoader()
        try {
            if (kind == VideoQualityKind.STREAMING) {
                saveStreamingQuality.executeSync(ip = ip, resolution = resolution, fps = fps)
            } else if (kind == VideoQualityKind.RECORDING) {
                saveRecordingQuality.executeSync(ip = ip, resolution = resolution, fps = fps)
            }
        } finally {
            loadingState.removeLoader()
        }
    }
}

