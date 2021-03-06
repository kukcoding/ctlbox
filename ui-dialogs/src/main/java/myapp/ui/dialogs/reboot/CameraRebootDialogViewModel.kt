package myapp.ui.dialogs.reboot

import android.content.Context
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import myapp.BuildVars
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.domain.interactors.RebootCamera
import myapp.flowInterval
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import myapp.util.tupleOf
import javax.inject.Inject


internal sealed class RebootStep {
    object None : RebootStep()
    object RebootingStarted : RebootStep()
    object FinishWait : RebootStep()
}


internal data class RebootDialogState(
    val step: RebootStep = RebootStep.None,
    val rebootStartTime: Long = 0L
)

private fun rebootProgress(startTime: Long): Float {
    val diff = System.currentTimeMillis() - startTime
    if (diff <= 0) return 0f
    val delta = diff / (BuildVars.rebootDurationSec * 1000f)
    return minOf(delta, 1f)
}

@HiltViewModel
internal class CameraRebootDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logger: Logger,
    val camManager: CamManager,
    private val rebootCamera: RebootCamera
) : ReduxViewModel<RebootDialogState>(RebootDialogState()) {
    private val loadingState = ObservableLoadingCounter()

    init {
        // 뷰모델 시작시 disconnect 메시지 비활성화
        // 뷰모델 onCleared()에서 disconnect 메시지 활성화
        camManager.disconnectedMessage.addDisableRequest()
    }

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()

    val rebootProgressLive = combine(
        flowFieldOf(RebootDialogState::step),
        flowFieldOf(RebootDialogState::rebootStartTime),
        ::tupleOf
    ).flatMapLatest { (step, startTime) ->
        if (step is RebootStep.RebootingStarted && startTime > 0) {
            flowInterval(0, 1000).map {
                (100 * rebootProgress(startTime = startTime)).toInt()
            }
        } else {
            flowOf(0)
        }
    }.distinctUntilChanged().asLiveData()


    val rebootTimeTextLive = combine(
        flowFieldOf(RebootDialogState::step),
        flowFieldOf(RebootDialogState::rebootStartTime),
        ::tupleOf
    ).flatMapLatest { (step, startTime) ->
        if (step is RebootStep.RebootingStarted && startTime > 0) {
            flowInterval(0, 1000).map {
                String.format("%d초 / %d초", (System.currentTimeMillis() - startTime) / 1000, BuildVars.rebootDurationSec)
            }
        } else {
            flowOf("")
        }
    }.distinctUntilChanged().asLiveData()


    suspend fun reboot(ip: String) {
        loadingState.addLoader()
        try {
            rebootCamera.executeSync(ip = ip)
        } finally {
            loadingState.removeLoader()
        }

        setState {
            copy(step = RebootStep.RebootingStarted, rebootStartTime = System.currentTimeMillis())
        }

        var remainSec = BuildVars.rebootDurationSec * 1000
        while (remainSec > 0 ) {
            delay(500)
            remainSec -= 500
        }
        setState { copy(step = RebootStep.FinishWait) }
    }

    override fun onCleared() {
        super.onCleared()
        camManager.disconnectedMessage.removeDisableRequest()
    }
}

