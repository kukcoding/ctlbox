package myapp.ui.dialogs.recordingschedule

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.data.code.RecordingScheduleType
import myapp.domain.interactors.SaveRecordingSchedule
import myapp.util.ObservableLoadingCounter
import myapp.util.tupleOf
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToLong


internal sealed class RecordingScheduleAction {
    data class Setup(
        val disabled: Boolean,
        val startTime: Instant,
        val durationMinute: Long
    ) : RecordingScheduleAction()

    data class SetStartDate(val year: Int, val month: Int, val day: Int) : RecordingScheduleAction()
    data class SetStartTime(val hour: Int, val minute: Int) : RecordingScheduleAction()
    data class SetFinishDate(val year: Int, val month: Int, val day: Int) : RecordingScheduleAction()
    data class SetFinishTime(val hour: Int, val minute: Int) : RecordingScheduleAction()
}


internal data class RecordingScheduleState(
    val scheduleType: RecordingScheduleType = RecordingScheduleType.DISABLED,
    val startTime: ZonedDateTime = ZonedDateTime.now(),
    val finishTime: ZonedDateTime? = null
) {
    val durationMinute: Long
        get() {
            return when (scheduleType) {
                RecordingScheduleType.DISABLED -> -1L
                RecordingScheduleType.INFINITE -> -1L
                RecordingScheduleType.FINITE -> {
                    if (finishTime == null) {
                        0L
                    } else {
                        val diffSeconds = finishTime.toEpochSecond() - startTime.toEpochSecond()
                        (diffSeconds / 60f).roundToLong()
                    }
                }
                else -> -1L
            }
        }
}

private fun formatDate(tm: ZonedDateTime): String {
    return "${tm.year}년 ${tm.monthValue}월 ${tm.dayOfMonth}일"
}

private fun formatTime(tm: ZonedDateTime): String {
    return "${tm.hour}시 ${tm.minute}분"
}

private fun replaceDate(tm: ZonedDateTime, year: Int, month: Int, day: Int): ZonedDateTime {
    return ZonedDateTime.of(
        year,
        month,
        day,
        tm.hour,
        tm.minute,
        0,
        0,
        tm.zone
    )
}

private fun replaceTime(tm: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
    return ZonedDateTime.of(
        tm.year,
        tm.monthValue,
        tm.dayOfMonth,
        hour,
        minute,
        0,
        0,
        tm.zone
    )
}


@HiltViewModel
internal class RecordingScheduleViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val camManager: CamManager,
    private val saveRecordingSchedule: SaveRecordingSchedule,
) : ReduxViewModel<RecordingScheduleState>(
    RecordingScheduleState()
) {
    private val pendingActions = Channel<RecordingScheduleAction>(Channel.BUFFERED)
    private val loadingState = ObservableLoadingCounter()

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()

    val isFiniteTypeLive = liveFieldOf(RecordingScheduleState::scheduleType).map { it == RecordingScheduleType.FINITE }
    val isInfiniteTypeLive =
        liveFieldOf(RecordingScheduleState::scheduleType).map { it == RecordingScheduleType.INFINITE }
    val isDisabledTypeLive =
        liveFieldOf(RecordingScheduleState::scheduleType).map { it == RecordingScheduleType.DISABLED }

    val startDateTextLive = liveFieldOf(RecordingScheduleState::startTime).map {
        formatDate(it)
    }

    val startTimeTextLive = liveFieldOf(RecordingScheduleState::startTime).map {
        formatTime(it)
    }

    val endDateTextLive = liveFieldOf(RecordingScheduleState::finishTime).map { dtm ->
        dtm?.let { formatDate(it) }
    }

    val endTimeTextLive = liveFieldOf(RecordingScheduleState::finishTime).map { dtm ->
        dtm?.let { formatTime(it) }
    }


    val errorMsgLive: LiveData<String?> = combine(
        flowFieldOf(RecordingScheduleState::scheduleType),
        flowFieldOf(RecordingScheduleState::startTime),
        flowFieldOf(RecordingScheduleState::finishTime),
        flowFieldOf(RecordingScheduleState::durationMinute),
        ::tupleOf
    ).map { (scheduleType, startTime, finishTime, durationMinute) ->
        Timber.w("XXX errorMsg $scheduleType $startTime $finishTime $durationMinute")
        if (scheduleType == RecordingScheduleType.FINITE) {
            when (finishTime) {
                null -> "종료시간을 선택해주세요"
                else -> {
                    val from = startTime.truncatedTo(ChronoUnit.MINUTES)
                    val to = finishTime.truncatedTo(ChronoUnit.MINUTES)
                    if (to.isBefore(from) || to.isEqual(from)) {
                        "녹화 종료 시간을 시작 시간보다 크게 입력해주세요"
                    } else {
                        null
                    }
                }
            }
        } else {
            null
        }
    }.asLiveData()

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
                    is RecordingScheduleAction.Setup -> updateSetup(
                        disabled = action.disabled,
                        startTime = action.startTime,
                        durationMinute = action.durationMinute
                    )
                    is RecordingScheduleAction.SetStartDate -> {
                        setState {
                            copy(startTime = replaceDate(startTime, action.year, action.month, action.day))
                        }
                    }
                    is RecordingScheduleAction.SetStartTime -> {
                        setState {
                            copy(startTime = replaceTime(startTime, action.hour, action.minute))
                        }
                    }
                    is RecordingScheduleAction.SetFinishDate -> {
                        setState {
                            val finishTime = finishTime ?: ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                            copy(finishTime = replaceDate(finishTime, action.year, action.month, action.day))
                        }
                    }
                    is RecordingScheduleAction.SetFinishTime -> {
                        setState {
                            val finishTime = finishTime ?: ZonedDateTime.now()
                            copy(finishTime = replaceTime(finishTime, action.hour, action.minute))
                        }
                    }
                }
            }
        }
    }


    fun submitAction(vararg actions: RecordingScheduleAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                actions.forEach { pendingActions.send(it) }
            }
        }
    }

    private fun updateSetup(disabled: Boolean, startTime: Instant, durationMinute: Long) {
        val scheduleType: RecordingScheduleType = when {
            disabled -> RecordingScheduleType.DISABLED
            durationMinute <= 0 -> RecordingScheduleType.INFINITE
            else -> RecordingScheduleType.FINITE
        }

        val startTimestamp = startTime.atZone(ZoneId.systemDefault())
        val endTimestamp = if (durationMinute > 0) {
            startTimestamp.plusMinutes(durationMinute)
        } else {
            startTimestamp.plusMinutes(5)
        }

        viewModelScope.launch {
            setState {
                copy(
                    scheduleType = scheduleType,
                    startTime = startTimestamp,
                    finishTime = endTimestamp
                )
            }
        }
    }

    suspend fun doSaveSchedule(ip: String, disabled: Boolean, startTime: Instant, durationMinute: Long) {
        loadingState.addLoader()
        try {
            saveRecordingSchedule.executeSync(
                ip = ip,
                disabled = disabled,
                startTime = startTime,
                durationMinute = durationMinute
            )
        } finally {
            loadingState.removeLoader()
        }
    }

    fun updateTypeFinite() {
        viewModelScope.launch {
            setState { copy(scheduleType = RecordingScheduleType.FINITE) }
        }
    }

    fun updateTypeInfinite() {
        viewModelScope.launch {
            setState { copy(scheduleType = RecordingScheduleType.INFINITE) }
        }
    }

    fun updateTypeDisabled() {
        viewModelScope.launch {
            setState { copy(scheduleType = RecordingScheduleType.DISABLED) }
        }
    }
}

