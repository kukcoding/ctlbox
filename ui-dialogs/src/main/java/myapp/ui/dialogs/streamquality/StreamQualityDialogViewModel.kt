package myapp.ui.dialogs.streamquality

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal sealed class StreamQualityAction {
    object Dummy : StreamQualityAction()
}

internal data class StreamQualityViewState(
    val availableResolutions: List<String> = emptyList(),
    val resolution: String = "1280x720",
    val fps: Int = 15,
)


@HiltViewModel
internal class StreamQualityDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val camManager: CamManager,
) : ReduxViewModel<StreamQualityViewState>(
    StreamQualityViewState()
) {
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
            camManager.observeConfig().collect { cfg ->
                if (cfg == null) {
                    setState { copy(availableResolutions = emptyList()) }
                } else {
                    setState {
                        copy(
                            availableResolutions = cfg.recordingResolutions,
                            resolution = cfg.recording.resolution,
                            fps = cfg.recording.fps
                        )
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

    fun updateResolution(resolution: String) {
        viewModelScope.launch {
            setState { copy(resolution = resolution) }
        }
    }

    fun updateResolution1920() {
        viewModelScope.launch {
            setState { copy(resolution = "1920x1080") }
        }
    }

}

