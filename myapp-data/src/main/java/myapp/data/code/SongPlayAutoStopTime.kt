package myapp.data.code


enum class SongPlayAutoStopTime(val title: String, val durationInSec: Long) {
    SEC_10("10초", 10),
    MINUTE_1("1분", 60),
    MINUTE_30("30분", 30 * 60),
    HOUR_1("1시간", 1 * 60 * 60),
    HOUR_2("2시간", 2 * 60 * 60),
    HOUR_3("3시간", 3 * 60 * 60),
    HOUR_4("4시간", 4 * 60 * 60),
    DISABLE("사용안함", 0)
    ;
}
