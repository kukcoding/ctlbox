package myapp.data.cam

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import myapp.data.entities.KuRecordingSchedule
import myapp.util.tupleOf
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import javax.inject.Inject


/**
 * 현재 녹화중인지 여부
 */
private fun isRecordingNow(schedule: KuRecordingSchedule): Boolean {
    if (schedule.disabled) return false
    val startTime = schedule.startTimestamp ?: return false

    val now = Instant.now()
    if (now.isBefore(startTime)) return false
    if (schedule.durationMinute <= 0L) return true
    val deadline = startTime.plusSeconds(60 * schedule.durationMinute)
    return now.isBefore(deadline)
}

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
                // 녹화 중이므로 시간이 만료될때까지 반복 체크한다
                flow {
                    emit(state)
                    delay(1000)
                    refreshFlow.tryEmit(System.currentTimeMillis())
                }
            }
            else -> flowOf(state)
        }
    }


    private fun waitRecordingStartFlow(schedule: KuRecordingSchedule): Flow<Boolean> {
        return flow {
            while (currentCoroutineContext().isActive) {
                val isRecording = isRecordingNow(schedule)
                emit(isRecording)
                if (isRecording) break
                delay(1000)
            }
        }
    }

    private fun waitRecordingFinishFlow(schedule: KuRecordingSchedule): Flow<Boolean> {
        return flow {
            while (currentCoroutineContext().isActive) {
                val isRecording = isRecordingNow(schedule)
                emit(isRecording)
                if (!isRecording) break
                delay(1000)
            }
        }
    }

    /**
     * 녹화 여부 flow
     * 녹화중이거나 녹화 예약 상태인 경우 1초간격으로 체크한다
     */
    val flow: Flow<Boolean> = stateFlow.flatMapLatest { state ->
        when (state) {
            is RecordingState.InfiniteRecording -> flowOf(true)
            is RecordingState.FiniteRecording -> flowOf(true)
            is RecordingState.RecordingScheduled -> flowOf(true)
            else -> flowOf(false)
        }
    }
}

private fun formatDate(startTime: Instant): String {
    val from = startTime.atZone(ZoneId.systemDefault())
    return "${from.year}년 ${from.monthValue}월 ${from.dayOfMonth}일 ${from.hour}시 ${from.minute}분 ${from.second}초"
}
