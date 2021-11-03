package myapp.extensions

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime


fun String.toYyyymmddDate() = try {
    LocalDate.of(this.substring(0, 4).toInt(), this.substring(4, 6).toInt(), this.substring(6, 8).toInt())
} catch (e: Throwable) {
    null
}

fun String.toYymmddDate() = try {
    LocalDate.of(this.substring(0, 2).toInt(), this.substring(2, 4).toInt(), this.substring(4, 6).toInt())
} catch (e: Throwable) {
    null
}

fun String.toHhmissTime() = try {
    LocalTime.of(this.substring(0, 2).toInt(), this.substring(2, 4).toInt(), this.substring(4, 6).toInt())
} catch (e: Throwable) {
    null
}

fun String.toHhmiTime() = try {
    LocalTime.of(this.substring(0, 2).toInt(), this.substring(2, 4).toInt(), 0)
} catch (e: Throwable) {
    null
}

fun String.toYyyymmddhhmissDateTime() = try {
    LocalDateTime.of(
        this.substring(0, 4).toInt(), this.substring(4, 6).toInt(), this.substring(6, 8).toInt(),
        this.substring(8, 10).toInt(), this.substring(10, 12).toInt(), this.substring(12, 14).toInt()
    )
} catch (e: Throwable) {
    null
}
