package myapp.ui.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import myapp.ReduxViewModel
import myapp.api.UiError
import myapp.ui.SnackbarManager
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter


internal sealed class LivePlayerAction {
    object ToggleEditing : LivePlayerAction()
}


internal data class LivePlayerState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val error: UiError? = null,
)

//@HiltViewModel
internal class LivePlayerViewModel @AssistedInject constructor(
    @ApplicationContext context: Context,
    private val snackbarManager: SnackbarManager,
    private val logger: Logger,
    @Assisted val videoUri: Uri,
) : ReduxViewModel<LivePlayerState>(LivePlayerState()) {
    private val loadingState = ObservableLoadingCounter()
    private val pendingActions = Channel<LivePlayerAction>(Channel.BUFFERED)

    val isLoadingLive = liveFieldOf(LivePlayerState::isLoading)

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
                    is LivePlayerAction.ToggleEditing -> setState { copy(isEditing = !isEditing) }
                }
            }
        }
    }

    fun submitAction(action: LivePlayerAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                pendingActions.send(action)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(videoUri: Uri): LivePlayerViewModel
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
