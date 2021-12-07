package myapp.data.entities

import kotlinx.parcelize.IgnoredOnParcel
import myapp.extensions.twoDigit
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

data class KuRecordFile(
    var fileId: String,
    val dateTime: LocalDateTime,
    val width: Int,
    val height: Int,
    val fps: Int,
    val bitrate: Int,// kbps
    val durationMilli: Long,
    val fileSize: Long,
) {
    @IgnoredOnParcel
    val yyyymmddhhmiss: String
        get() = "${dateTime.year}${dateTime.monthValue.twoDigit()}${dateTime.dayOfMonth.twoDigit()}${dateTime.hour.twoDigit()}${dateTime.minute.twoDigit()}${dateTime.second.twoDigit()}"

    @IgnoredOnParcel
    val filterKey: String
        get() = "${dateTime.year}${dateTime.monthValue.twoDigit()}${dateTime.dayOfMonth.twoDigit()}${dateTime.hour.twoDigit()}"

    @IgnoredOnParcel
    val galleryFileName: String
        get() = "${dateTime.year}년${dateTime.monthValue.twoDigit()}월${dateTime.dayOfMonth.twoDigit()}일_${dateTime.hour.twoDigit()}시${dateTime.minute.twoDigit()}분_${width}x${height}.mp4"

    companion object {
        private val SEPARATOR_REGEX = "[_x.]".toRegex()

        // ${epochSecond}_${width}x${height}_${fps}_${kbps}_${duration_msec}_${file_size}.mp4
        // "20211012_180556_3840x2160_15_1500_33327_7562350.mp4",
        // "20211012_180737_3840x2160_15_1500_0_0.mp4", # <-- means that file is recording now (not closed)
        // 12312312_3840x2160_15_1500_33372_7558826.mp4",
        fun createFromFileId(fileId: String): KuRecordFile {
            val parts = fileId.split(SEPARATOR_REGEX)
            val second = parts[0].toLong()
            val width = parts[1].toInt()
            val height = parts[2].toInt()
            val fps = parts[3].toInt()
            val bitrate = parts[4].toInt()
            val durationMilli = parts[5].toLong()
            val fileSize = parts[6].toLong()
            Instant.ofEpochSecond(second).atZone(ZoneId.systemDefault()).toLocalDateTime()
            return KuRecordFile(
                fileId = fileId,
                dateTime = Instant.ofEpochSecond(second).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                width = width,
                height = height,
                fps = fps,
                bitrate = bitrate,
                durationMilli = durationMilli,
                fileSize = fileSize
            )
        }
    }
}
