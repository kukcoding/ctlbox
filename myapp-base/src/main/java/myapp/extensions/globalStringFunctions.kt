package myapp.extensions

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.math.*

/**
 * 비교 횟수를 줄이기 위해 메소드에 디폴트 값을 지정하지 않았음
 */

fun firstNotEmpty(v1: String?, v2: String?): String? {
    if (!v1.isNullOrEmpty()) return v1
    if (!v2.isNullOrEmpty()) return v2
    return null
}

fun firstNotEmpty(v1: String?, v2: String?, v3: String?): String? {
    if (!v1.isNullOrEmpty()) return v1
    if (!v2.isNullOrEmpty()) return v2
    if (!v3.isNullOrEmpty()) return v3
    return null
}

fun firstNotEmpty(v1: String?, v2: String?, v3: String?, v4: String?): String? {
    if (!v1.isNullOrEmpty()) return v1
    if (!v2.isNullOrEmpty()) return v2
    if (!v3.isNullOrEmpty()) return v3
    if (!v4.isNullOrEmpty()) return v4
    return null
}

fun firstNotEmpty(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?): String? {
    if (!v1.isNullOrEmpty()) return v1
    if (!v2.isNullOrEmpty()) return v2
    if (!v3.isNullOrEmpty()) return v3
    if (!v4.isNullOrEmpty()) return v4
    if (!v5.isNullOrEmpty()) return v5
    return null
}

fun firstNotEmpty(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?, v6: String?): String? {
    if (!v1.isNullOrEmpty()) return v1
    if (!v2.isNullOrEmpty()) return v2
    if (!v3.isNullOrEmpty()) return v3
    if (!v4.isNullOrEmpty()) return v4
    if (!v5.isNullOrEmpty()) return v5
    if (!v6.isNullOrEmpty()) return v6
    return null
}

fun firstNotBlank(v1: String?, v2: String?): String? {
    if (!v1.isNullOrBlank()) return v1
    if (!v2.isNullOrBlank()) return v2
    return null
}


fun firstNotBlank(v1: String?, v2: String?, v3: String?): String? {
    if (!v1.isNullOrBlank()) return v1
    if (!v2.isNullOrBlank()) return v2
    if (!v3.isNullOrBlank()) return v3
    return null
}

fun firstNotBlank(v1: String?, v2: String?, v3: String?, v4: String?): String? {
    if (!v1.isNullOrBlank()) return v1
    if (!v2.isNullOrBlank()) return v2
    if (!v3.isNullOrBlank()) return v3
    if (!v4.isNullOrBlank()) return v4
    return null
}


fun firstNotBlank(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?): String? {
    if (!v1.isNullOrBlank()) return v1
    if (!v2.isNullOrBlank()) return v2
    if (!v3.isNullOrBlank()) return v3
    if (!v4.isNullOrBlank()) return v4
    if (!v5.isNullOrBlank()) return v5
    return null
}

fun firstNotBlank(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?, v6: String?): String? {
    if (!v1.isNullOrBlank()) return v1
    if (!v2.isNullOrBlank()) return v2
    if (!v3.isNullOrBlank()) return v3
    if (!v4.isNullOrBlank()) return v4
    if (!v5.isNullOrBlank()) return v5
    if (!v6.isNullOrBlank()) return v6
    return null
}

fun <T : Any> firstNotNull(v1: T?, v2: T?): T? {
    if (v1 != null) return v1
    if (v2 != null) return v2
    return null
}


fun <T : Any> firstNotNull(v1: T?, v2: T?, v3: T?): T? {
    if (v1 != null) return v1
    if (v2 != null) return v2
    if (v3 != null) return v3
    return null
}

fun <T : Any> firstNotNull(v1: T?, v2: T?, v3: T?, v4: T?): T? {
    if (v1 != null) return v1
    if (v2 != null) return v2
    if (v3 != null) return v3
    if (v4 != null) return v4
    return null
}


fun <T : Any> firstNotNull(v1: T?, v2: T?, v3: T?, v4: T?, v5: T?): T? {
    if (v1 != null) return v1
    if (v2 != null) return v2
    if (v3 != null) return v3
    if (v4 != null) return v4
    if (v5 != null) return v5
    return null
}

fun <T : Any> firstNotNull(v1: T?, v2: T?, v3: T?, v4: T?, v5: T?, v6: T?): T? {
    if (v1 != null) return v1
    if (v2 != null) return v2
    if (v3 != null) return v3
    if (v4 != null) return v4
    if (v5 != null) return v5
    if (v6 != null) return v6
    return null
}

fun isNotNullAll(v1: Any?, v2: Any?): Boolean {
    return v1 != null && v2 != null
}

fun isNotNullAll(v1: Any?, v2: Any?, v3: Any?): Boolean {
    return v1 != null && v2 != null && v3 != null
}

fun isNotNullAll(v1: Any?, v2: Any?, v3: Any?, v4: Any?): Boolean {
    return v1 != null && v2 != null && v3 != null && v4 != null
}

fun isNotNullAll(v1: Any?, v2: Any?, v3: Any?, v4: Any?, v5: Any?): Boolean {
    return v1 != null && v2 != null && v3 != null && v4 != null && v5 != null
}

fun isNotNullAll(v1: Any?, v2: Any?, v3: Any?, v4: Any?, v5: Any?, v6: Any?): Boolean {
    return v1 != null && v2 != null && v3 != null && v4 != null && v5 != null && v6 != null
}


fun isNotNullAny(v1: Any?, v2: Any?): Boolean {
    return v1 != null || v2 != null
}

fun isNotNullAny(v1: Any?, v2: Any?, v3: Any?): Boolean {
    return v1 != null || v2 != null || v3 != null
}

fun isNotNullAny(v1: Any?, v2: Any?, v3: Any?, v4: Any?): Boolean {
    return v1 != null || v2 != null || v3 != null || v4 != null
}

fun isNotNullAny(v1: Any?, v2: Any?, v3: Any?, v4: Any?, v5: Any?): Boolean {
    return v1 != null || v2 != null || v3 != null || v4 != null || v5 != null
}

fun isNotNullAny(v1: Any?, v2: Any?, v3: Any?, v4: Any?, v5: Any?, v6: Any?): Boolean {
    return v1 != null || v2 != null || v3 != null || v4 != null || v5 != null || v6 != null
}


fun isBlankAll(v1: String?, v2: String?): Boolean {
    return v1.isNullOrBlank() &&
        v2.isNullOrBlank()
}


fun isBlankAll(v1: String?, v2: String?, v3: String?): Boolean {
    return v1.isNullOrBlank() &&
        v2.isNullOrBlank() &&
        v3.isNullOrBlank()
}

fun isBlankAll(v1: String?, v2: String?, v3: String?, v4: String?): Boolean {
    return v1.isNullOrBlank() &&
        v2.isNullOrBlank() &&
        v3.isNullOrBlank() &&
        v4.isNullOrBlank()
}


fun isBlankAll(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?): Boolean {
    return v1.isNullOrBlank() &&
        v2.isNullOrBlank() &&
        v3.isNullOrBlank() &&
        v4.isNullOrBlank() &&
        v5.isNullOrBlank()
}

fun isBlankAll(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?, v6: String?): Boolean {
    return v1.isNullOrBlank() &&
        v2.isNullOrBlank() &&
        v3.isNullOrBlank() &&
        v4.isNullOrBlank() &&
        v5.isNullOrBlank() &&
        v6.isNullOrBlank()
}


fun isBlankAny(v1: String?, v2: String?): Boolean {
    return v1.isNullOrBlank() ||
        v2.isNullOrBlank()
}

fun isBlankAny(v1: String?, v2: String?, v3: String?): Boolean {
    return v1.isNullOrBlank() ||
        v2.isNullOrBlank() ||
        v3.isNullOrBlank()
}

fun isBlankAny(v1: String?, v2: String?, v3: String?, v4: String?): Boolean {
    return v1.isNullOrBlank() ||
        v2.isNullOrBlank() ||
        v3.isNullOrBlank() ||
        v4.isNullOrBlank()
}

fun isBlankAny(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?): Boolean {
    return v1.isNullOrBlank() ||
        v2.isNullOrBlank() ||
        v3.isNullOrBlank() ||
        v4.isNullOrBlank() ||
        v5.isNullOrBlank()
}


fun isBlankAny(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?, v6: String?): Boolean {
    return v1.isNullOrBlank() ||
        v2.isNullOrBlank() ||
        v3.isNullOrBlank() ||
        v4.isNullOrBlank() ||
        v5.isNullOrBlank() ||
        v6.isNullOrBlank()
}


fun isEmptyAll(v1: String?, v2: String?): Boolean {
    return v1.isNullOrEmpty() &&
        v2.isNullOrEmpty()
}

fun isEmptyAll(v1: String?, v2: String?, v3: String?): Boolean {
    return v1.isNullOrEmpty() &&
        v2.isNullOrEmpty() &&
        v3.isNullOrEmpty()
}


fun isEmptyAll(v1: String?, v2: String?, v3: String?, v4: String?): Boolean {
    return v1.isNullOrEmpty() &&
        v2.isNullOrEmpty() &&
        v3.isNullOrEmpty() &&
        v4.isNullOrEmpty()
}

fun isEmptyAll(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?): Boolean {
    return v1.isNullOrEmpty() &&
        v2.isNullOrEmpty() &&
        v3.isNullOrEmpty() &&
        v4.isNullOrEmpty() &&
        v5.isNullOrEmpty()
}

fun isEmptyAll(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?, v6: String?): Boolean {
    return v1.isNullOrEmpty() &&
        v2.isNullOrEmpty() &&
        v3.isNullOrEmpty() &&
        v4.isNullOrEmpty() &&
        v5.isNullOrEmpty() &&
        v6.isNullOrEmpty()
}

fun isEmptyAny(v1: String?, v2: String?): Boolean {
    return v1.isNullOrEmpty() ||
        v2.isNullOrEmpty()
}

fun isEmptyAny(v1: String?, v2: String?, v3: String?): Boolean {
    return v1.isNullOrEmpty() ||
        v2.isNullOrEmpty() ||
        v3.isNullOrEmpty()
}

fun isEmptyAny(v1: String?, v2: String?, v3: String?, v4: String?): Boolean {
    return v1.isNullOrEmpty() ||
        v2.isNullOrEmpty() ||
        v3.isNullOrEmpty() ||
        v4.isNullOrEmpty()
}

fun isEmptyAny(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?): Boolean {
    return v1.isNullOrEmpty() ||
        v2.isNullOrEmpty() ||
        v3.isNullOrEmpty() ||
        v4.isNullOrEmpty() ||
        v5.isNullOrEmpty()
}

fun isEmptyAny(v1: String?, v2: String?, v3: String?, v4: String?, v5: String?, v6: String?): Boolean {
    return v1.isNullOrEmpty() ||
        v2.isNullOrEmpty() ||
        v3.isNullOrEmpty() ||
        v4.isNullOrEmpty() ||
        v5.isNullOrEmpty() ||
        v6.isNullOrEmpty()
}

/**
 * emptyToNull("  a  ") = "  a  "
 * emptyToNull(null)    = null
 * emptyToNull("")      = null
 * emptyToNull("    ")  = "    "
 */
fun emptyToNull(v: String?): String? {
    if (v.isNullOrEmpty()) return null
    return v
}


/**
 * blankToNull("  a  ") = "  a  "
 * blankToNull(null) = null
 * blankToNull("   ") = null
 */
fun blankToNull(v: String?): String? {
    if (v.isNullOrBlank()) return null
    return v
}

/**
 * trimNull("  a  ") = "a"
 * trimNull(null) = null
 * trimNull("   ") = null
 */
fun trimNull(v: CharSequence?): String? {
    if (v.isNullOrBlank()) return null
    return v.trim().toString()
}

/**
 * trimEmpty("  a  ") = "a"
 * trimEmpty(null) = ""
 * trimEmpty("   ") = ""
 */
fun trimEmpty(v: CharSequence?): String {
    if (v.isNullOrBlank()) return ""
    return v.trim().toString()
}


fun humanReadableSize(size: Int): String {
    return humanReadableSize(size.toLong())
}

fun humanReadableSize(size: Long, fmt: String = "#,##0.#"): String {
    if (size <= 0) return "0"
    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat(fmt).format(
        size / 1024.0.pow(digitGroups.toDouble())
    ) + " " + units[digitGroups]
}

fun humanReadableCount(number: Int): String {
    return humanReadableCount(number.toLong())
}

fun humanReadableCount(number: Long): String {
    if (number < 1000)
        return "" + number
    val exp = (ln(number.toDouble()) / ln(1000.0)).toInt()
    return String.format("%.1f %c", number / 1000.0.pow(exp.toDouble()), "kMGTPE"[exp - 1])
}


fun formatDurationSecond(duration: Float, text: Boolean = false): String? {
    return formatDurationMilli(round(duration * 1000).toLong(), text)
}

//fun fancyResolution(resolution: String): String {
//    return try {
//        val value = resolution.split("""\s*x\s*""".toRegex()).maxOf { it.toInt() }
//        when {
//            value > 3000 -> "4K (${resolution})"
//            value == 1920 -> "FHD (${resolution})"
//            value == 1280 -> "HD (${resolution})"
//            else -> resolution
//        }
//    } catch (e: Throwable) {
//        resolution
//    }
//}

fun fancyResolution(resolution: String): String? {
    return try {
        val value = resolution.split("""\s*x\s*""".toRegex()).maxOf { it.toInt() }
        when {
            value > 3000 -> "4K"
            value == 1920 -> "FHD"
            value == 1280 -> "HD"
            else -> null
        }
    } catch (e: Throwable) {
        null
    }
}

fun formatDurationMilli(durationMilli: Long, text: Boolean = false): String {
    var millis = durationMilli
    val negative = millis < 0
    millis = abs(millis)
    val sec = (millis / 1000f).roundToInt() % 60
    millis /= 60000
    val min = (millis % 60).toInt()
    millis /= 60
    val hours = millis.toInt()
    val format = NumberFormat.getInstance(Locale.US) as DecimalFormat
    format.applyPattern("00")
    return if (text) {
        return if ("ko" == Locale.getDefault().language) {
            val sb = StringBuilder()
            if (hours > 0) sb.append(hours).append("시간 ")
            if (min > 0 || sb.isNotEmpty()) sb.append(min).append("분 ")
            sb.append(sec).append("초")
            (if (negative) "-" else "") + sb.toString()
        } else {
            val sb = StringBuilder()
            if (hours > 0) sb.append(hours).append("h ")
            if (min > 0 || sb.isNotEmpty()) sb.append(min).append("min ")
            sb.append(sec).append("s")
            (if (negative) "-" else "") + sb.toString()
        }
    } else {
        if (millis > 0) (if (negative) "-" else "") + hours + ":" + format.format(min.toLong()) + ":" + format.format(
            sec.toLong()
        ) else (if (negative) "-" else "") + min + ":" + format.format(sec.toLong())
    }
}


fun formatDurationMilliInMinute(durationMilli: Long, text: Boolean = false): String {
    var millis = durationMilli
    val negative = millis < 0
    millis = abs(millis)
    val sec = (millis / 1000f).roundToInt() % 60
    millis /= 60000
    val minute = (millis % 60).toInt()
    millis /= 60
    val hours = millis.toInt()
    val format = NumberFormat.getInstance(Locale.US) as DecimalFormat
    format.applyPattern("00")
    return if (text) {
        return if ("ko" == Locale.getDefault().language) {
            val sb = StringBuilder()
            if (hours > 0) sb.append(hours).append("시간 ")
            sb.append(minute).append("분")
            (if (negative) "-" else "") + sb.toString()
        } else {
            val sb = StringBuilder()
            if (hours > 0) sb.append(hours).append("h ")
            sb.append(minute).append("min")
            (if (negative) "-" else "") + sb.toString()
        }
    } else {
        if (millis > 0) {
            (if (negative) "-" else "") + hours + ":" + format.format(minute.toLong())
        } else {
            (if (negative) "-" else "") + minute + ":" + format.format(sec.toLong())
        }
    }
}


public fun CharSequence?.trimOrEmpty(): CharSequence {
    return this?.trim() ?: return ""
}

public fun CharSequence?.trimOrNull(): CharSequence? {
    val txt = this?.trim() ?: return null
    if (txt.isEmpty()) return null
    return txt
}

