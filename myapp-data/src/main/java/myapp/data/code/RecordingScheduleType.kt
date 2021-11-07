package myapp.data.code


enum class RecordingScheduleType(val desc: String) {
    DISABLED("사용안함"), FINITE("녹화 시간 지정"), INFINITE("상시 녹확")
}
