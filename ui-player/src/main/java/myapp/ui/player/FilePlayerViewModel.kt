package myapp.ui.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kr.ohlab.android.recyclerviewgroup.ItemBase
import myapp.ReduxViewModel
import myapp.api.UiError
import myapp.ui.SnackbarManager
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter

internal sealed class FilePlayerAction {
    object ToggleEditing : FilePlayerAction()
}


internal data class FilePlayerState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val error: UiError? = null,
)

//@HiltViewModel
internal class FilePlayerViewModel @AssistedInject constructor(
    private val snackbarManager: SnackbarManager,
    private val logger: Logger,
    @Assisted val videoUri: Uri,
) : ReduxViewModel<FilePlayerState>(FilePlayerState()) {
    private val loadingState = ObservableLoadingCounter()
    private val pendingActions = Channel<FilePlayerAction>(Channel.BUFFERED)

    val viewItems = MutableLiveData<List<ItemBase<*>>>()
    val isLoadingLive = liveFieldOf(FilePlayerState::isLoading)

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

        // 액션 처리
        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is FilePlayerAction.ToggleEditing -> setState { copy(isEditing = !isEditing) }
                }
            }
        }
    }

    fun submitAction(action: FilePlayerAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                pendingActions.send(action)
            }
        }
    }

    fun mediaSource(appCtx: Context): MediaSource {
        return if (this.videoUri.toString().startsWith("rtsp://")) {
            RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(this.videoUri))
        } else {
            ProgressiveMediaSource.Factory(DefaultDataSourceFactory(appCtx))
                .createMediaSource(MediaItem.fromUri(this.videoUri))
        }
    }


    @AssistedFactory
    interface Factory {
        fun create(videoUri: Uri): FilePlayerViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            videoUri: Uri
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(videoUri) as T
            }
        }
    }
}
