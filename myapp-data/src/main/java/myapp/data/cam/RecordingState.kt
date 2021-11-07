package myapp.data.cam

import myapp.data.entities.KuRecordingSchedule

sealed class RecordingState {
    data class Disabled(val schedule: KuRecordingSchedule?) : RecordingState()
    data class FiniteRecording(val schedule: KuRecordingSchedule) : RecordingState()
    data class InfiniteRecording(val schedule: KuRecordingSchedule) : RecordingState()
    data class RecordingScheduled(val schedule: KuRecordingSchedule) : RecordingState()
    data class RecordingExpired(val schedule: KuRecordingSchedule) : RecordingState()
}
