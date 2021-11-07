package myapp.data.cam

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import myapp.util.tupleOf
import org.threeten.bp.Instant
import javax.inject.Inject


class RecordingTracker @Inject constructor(
    camManager: CamManager
) {

    private val refreshFlow = MutableStateFlow(0L)

    /**
     * 녹화 여부 flow
     * 녹화중이거나 녹화 예약 상태인 경우 1초간격으로 체크한다
     */
    val stateFlow: Flow<RecordingState> = combine(
        camManager.observeConfig().map { it?.recordingSchedule },
        refreshFlow,
        ::tupleOf
    ).map { (schedule, _) ->
        if (schedule == null) {
            return@map RecordingState.Disabled(null)
        }
        val startTime = schedule.startTimestamp
        val durationMinute = schedule.durationMinute
        when {
            startTime == null -> RecordingState.Disabled(null)
            schedule.disabled -> RecordingState.Disabled(null)
            else -> {
                val now = Instant.now()
                if (now.isBefore(startTime)) {
                    RecordingState.RecordingScheduled(schedule)
                } else {
                    if (durationMinute <= 0) {
                        RecordingState.InfiniteRecording(schedule)
                    } else {
                        val deadline = startTime.plusSeconds(60 * durationMinute)
                        if (now.isBefore(deadline)) {
                            RecordingState.FiniteRecording(schedule)
                        } else {
                            RecordingState.RecordingExpired(schedule)
                        }
                    }
                }
            }
        }
    }.flatMapLatest { state ->
        when (state) {
            is RecordingState.FiniteRecording,
            is RecordingState.RecordingScheduled -> {
                // 반복 체크한다
                flow {
                    emit(state)
                    delay(1000)
                    refreshFlow.tryEmit(System.currentTimeMillis())
                }
            }
            else -> flowOf(state)
        }
    }

    /**
     * 녹화 여부 flow
     * 녹화중이거나 녹화 예약 상태인 경우 1초간격으로 체크한다
     */
    val isRecordingFlow: Flow<Boolean> = stateFlow.flatMapLatest { state ->
        when (state) {
            is RecordingState.InfiniteRecording -> flowOf(true)
            is RecordingState.FiniteRecording -> flowOf(true)
            is RecordingState.RecordingScheduled -> flowOf(false)
            else -> flowOf(false)
        }
    }
}
