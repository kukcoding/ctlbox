package myapp.extensions

import android.app.Activity
import android.content.res.Resources
import android.graphics.Color
import androidx.annotation.ColorInt
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.YearMonth


inline fun <reified T : Any> Activity.extraOrNull(key: String, default: T? = null) = lazy {
    val value = intent?.extras?.get(key)
    if (value is T) value else default
}

inline fun <reified T : Any> Activity.extraNotNull(key: String, default: T? = null) = lazy {
    val value = intent?.extras?.get(key)
    requireNotNull(if (value is T) value else default) { key }
}


fun requireNotNulls(vararg any: Any?) {
    any.forEach {
        requireNotNull(value = it)
    }
}

@ColorInt
fun String.toHexColor() = Color.parseColor(this)


fun min(date1: LocalDate, date2: LocalDate) = if (date1.isBefore(date2)) date1 else date2
fun max(date1: LocalDate, date2: LocalDate) = if (date1.isAfter(date2)) date1 else date2

fun min(date1: LocalDateTime, date2: LocalDateTime) = if (date1.isBefore(date2)) date1 else date2
fun max(date1: LocalDateTime, date2: LocalDateTime) = if (date1.isAfter(date2)) date1 else date2

fun min(date1: Instant, date2: Instant) = if (date1.isBefore(date2)) date1 else date2
fun max(date1: Instant, date2: Instant) = if (date1.isAfter(date2)) date1 else date2

fun LocalDate.yymmdd() = String.format("%02d%02d%02d", this.year % 100, this.monthValue, this.dayOfMonth)
fun LocalDate.yyyymmdd() = String.format("%04d%02d%02d", this.year, this.monthValue, this.dayOfMonth)
fun LocalDateTime.yyyymmdd() = String.format("%04d%02d%02d", this.year, this.monthValue, this.dayOfMonth)
fun LocalDateTime.yyyymmddhhmiss() = String.format(
    "%04d%02d%02d%02d%02d%02d",
    this.year,
    this.monthValue,
    this.dayOfMonth,
    this.hour,
    this.minute,
    this.second
)

fun String.yyyymmddToLocalDate() =
    LocalDate.of(this.substring(0, 4).toInt(), this.substring(4, 6).toInt(), this.substring(6, 8).toInt())

fun String.yymmddToLocalDate() =
    LocalDate.of(this.substring(0, 2).toInt(), this.substring(2, 4).toInt(), this.substring(4, 6).toInt())

fun String.yyyymmToYearMonth() = YearMonth.of(this.substring(0, 4).toInt(), this.substring(4, 6).toInt())


fun String.Companion.fromYyyymmdd(year: Int, month: Int, day: Int) = String.format("%04d%02d%02d", year, month, day)

fun LocalDate.plusDays(days: Int) = this.plusDays(days.toLong())
fun LocalDate.minusDays(days: Int) = this.minusDays(days.toLong())


fun Int.twoDigit(): String {
    return if (this >= 10) this.toString()
    else "0$this"
}

val Int.dp: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
