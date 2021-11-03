package myapp.common.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter

// @JsonQualifier를 이용하는 법 참고,
// 포맷이 다른 Json 값을 동일한 타입의 객체로 변환하는 방법
// 예를 들어, 밀리초도 LocalDateTime으로, ISO_LOCAL_DATE_TIME 포맷의 문자열도 LocalDateTime으로 변경하고자 할때
// 아래글을 읽어보면 됨
// Moshi DateTime Adapter With Multiple Format Support
// https://code.luasoftware.com/tutorials/android/moshi-datetime-adapter-with-multiple-format-support/
//@Retention(AnnotationRetention.RUNTIME)
//@JsonQualifier
//internal annotation class DateString


object MyMoshiDateAdapters {

//    object InstantAdapter {
//        @ToJson
//        fun toJson(value: Instant): Long {
//            return value.toEpochMilli()
//        }
//
//        @FromJson
//        fun toJson(value: Long): Instant {
//            return Instant.ofEpochMilli(value)
//        }
//    }

//    object InstantAdapter {
//        private val FORMATTER = DateTimeFormatter.ISO_INSTANT
//
//        @ToJson
//        fun toJson(value: Instant): String {
//            return FORMATTER.format(value)
//        }
//
//        @FromJson
//        fun fromJson(value: String): Instant {
//            return FORMATTER.parse(value, Instant::from)
//        }
//    }

    object InstantAdapter {
        @ToJson
        fun toJson(value: Instant): String {
            return "${value.toEpochMilli() / 1000f}"
        }

        @FromJson
        fun fromJson(value: String): Instant {
            val timeValue = value.toDouble()
            val seconds = timeValue.toLong()
            val nano = ((timeValue - seconds) * 1_000_000_000).toLong()
            return Instant.ofEpochSecond(timeValue.toLong(), nano)
            // return FORMATTER.parse(value, Instant::from)
        }
    }

    object LocalDateAdapter {
        private val FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

        @ToJson
        fun toJson(value: LocalDate): String {
            return FORMATTER.format(value)
        }

        @FromJson
        fun fromJson(value: String): LocalDate {
            return FORMATTER.parse(value, LocalDate::from)
        }
    }


    object LocalDateTimeAdapter {
        private val FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        @ToJson
        fun toJson(value: LocalDateTime): String {
            return FORMATTER.format(value)
        }

        @FromJson
        fun fromJson(value: String): LocalDateTime {
            return FORMATTER.parse(value, LocalDateTime::from)
        }
    }


    object OffsetDateTimeAdapter {
        private val FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
        @ToJson
        fun toJson(value: OffsetDateTime): String {
            return FORMATTER.format(value)
        }

        @FromJson
        fun fromJson(value: String): OffsetDateTime {
            return FORMATTER.parse(value, OffsetDateTime::from)
        }
    }

    object ZonedDateTimeAdapter {
        private val FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME
        @ToJson
        fun toJson(value: ZonedDateTime): String {
            return FORMATTER.format(value)
        }

        @FromJson
        fun fromJson(value: String): ZonedDateTime {
            return FORMATTER.parse(value, ZonedDateTime::from)
        }
    }
}
