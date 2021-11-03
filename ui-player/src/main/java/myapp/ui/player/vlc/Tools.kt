package myapp.ui.player.vlc

import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import org.videolan.libvlc.util.VLCUtil
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern

object Tools {
    val TWO_DIGITS: ThreadLocal<NumberFormat> = object : ThreadLocal<NumberFormat>() {
        override fun initialValue(): NumberFormat {
            val fmt = NumberFormat.getInstance(Locale.US)
            if (fmt is DecimalFormat) fmt.applyPattern("00")
            return fmt
        }
    }

    /*
     * Convert file:// uri from real path to emulated FS path.
     */
    @JvmStatic
    fun convertLocalUri(uri: Uri): Uri {
        if (!TextUtils.equals(uri.scheme, "file") || !uri.path!!.startsWith("/sdcard")) return uri
        val path = uri.toString()
        return Uri.parse(path.replace("/sdcard", Environment.getExternalStorageDirectory().path))
    }

    fun isArrayEmpty(array: Array<Any?>?): Boolean {
        return array == null || array.size == 0
    }

    /**
     * Convert time to a string
     * @param millis e.g.time/length from file
     * @return formated string "[hh]h[mm]min" / "[mm]min[s]s"
     */
    fun millisToText(millis: Long): String {
        return millisToString(millis, true, true, false)
    }

    /**
     * Convert time to a string with large formatting
     *
     * @param millis e.g.time/length from file
     * @return formated string "[hh]h [mm]min " / "[mm]min [s]s"
     */
    fun millisToTextLarge(millis: Long): String {
        return millisToString(millis, true, true, true)
    }
    //    public static String getProgressText(MediaWrapper media) {
    //        long lastTime = media.getTime();
    //        if (lastTime <= 0L) return "";
    //        return String.format("%s / %s",
    //                millisToString(lastTime, true, false, false),
    //                millisToString(media.getLength(), true, false, false));
    //    }
    /**
     * Convert time to a string
     * @param millis e.g.time/length from file
     * @return formated string (hh:)mm:ss
     */
    @JvmOverloads
    @JvmStatic
    fun millisToString(
        millis: Long,
        text: Boolean = false,
        seconds: Boolean = true,
        large: Boolean = false
    ): String {
        var ms = millis
        val sb = StringBuilder()
        if (ms < 0) {
            ms = -ms
            sb.append("-")
        }
        ms /= 1000
        val sec = (ms % 60).toInt()
        ms /= 60
        val min = (ms % 60).toInt()
        ms /= 60
        val hours = ms.toInt()
        if (text) {
            if (hours > 0) sb.append(hours).append('h').append(if (large) " " else "")
            if (min > 0) sb.append(min).append("min").append(if (large) " " else "")
            if ((seconds || sb.isEmpty()) && sec > 0) sb.append(sec).append("s")
                .append(if (large) " " else "")
        } else {
            val fmt = TWO_DIGITS.get()!!
            if (hours > 0) sb.append(hours).append(':').append(if (large) " " else "").append(
                fmt.format(min.toLong())
            ).append(':').append(if (large) " " else "").append(
                fmt.format(sec.toLong())
            ) else sb.append(min).append(':').append(if (large) " " else "").append(
                fmt.format(sec.toLong())
            )
        }
        return sb.toString()
    }

    fun encodeVLCMrl(mrl: String): String {
        var mrl2 = mrl
        if (mrl2.startsWith("/")) mrl2 = "file://$mrl2"
        return VLCUtil.encodeVLCString(mrl2)
    }

    /**
     * Search in a case insensitive manner for a substring in a source string
     * @param source Source string in which to look for the substring
     * @param substring substring to search in the source string
     * @return presence of the substring as a boolean
     */
    fun hasSubString(source: String?, substring: String?): Boolean {
        return Pattern.compile(Pattern.quote(substring), Pattern.CASE_INSENSITIVE).matcher(source)
            .find()
    }
}
