package myapp.ui.splash


import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import myapp.ReduxViewModel
import myapp.data.entities.Result
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import org.threeten.bp.Instant
import javax.inject.Inject

internal data class SplashViewState(
    val isLoading: Boolean = false,
    val currentAction: SplashAction = SplashAction.FIRST,
    val hymnSyncResult: Result<Unit>? = null,
    val startTimestamp: Instant = Instant.now()
)

@HiltViewModel
internal class SplashViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logger: Logger
) : ReduxViewModel<SplashViewState>(SplashViewState()) {

    private val loadingState = ObservableLoadingCounter()
    private val pendingSplashActions = SplashAction.values().filter { it != SplashAction.FIRST }.toMutableList()
    val loadingTextLive = MutableLiveData("")

    init {
        logger.d("SplashViewModel created")
        viewModelScope.launch {
            setState { copy(startTimestamp = Instant.now()) }
        }

        viewModelScope.launch {
            loadingState.observable
                .distinctUntilChanged()
                .debounce(2000)
                .collectAndSetState { copy(isLoading = it) }
        }
    }

    fun nextAction() {
        viewModelScope.launch {
            val action = pendingSplashActions.removeFirstOrNull()
            if (action != null) {
                setState { copy(currentAction = action) }
            }
        }
    }


    suspend fun ensureSplashDelay() {
        val diff = System.currentTimeMillis() - currentState().startTimestamp.toEpochMilli()
        val delayTime = 3000 - diff
        if (delayTime > 0) {
            delay(delayTime)
        }
    }
}
