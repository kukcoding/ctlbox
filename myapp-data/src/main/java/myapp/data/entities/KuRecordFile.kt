package myapp.data.entities

import kotlinx.parcelize.IgnoredOnParcel
import myapp.extensions.twoDigit
import org.threeten.bp.LocalDateTime

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

        // ${yymmdd}_${hhmmss}_${width}x${height}_${fps}_${kbps}_${duration_msec}_${file_size}.mp4
        // "20211012_180556_3840x2160_15_1500_33327_7562350.mp4",
        // "20211012_180630_3840x2160_15_1500_33613_7548455.mp4",
        // "20211012_180703_3840x2160_15_1500_33372_7558826.mp4",
        // "20211012_180737_3840x2160_15_1500_0_0.mp4", # <-- means that file is recording now (not closed)
        //
        fun createFromFileId(fileId: String): KuRecordFile {
            val parts = fileId.split(SEPARATOR_REGEX)
            val ymd = parts[0]
            val hms = parts[1]
            val width = parts[2].toInt()
            val height = parts[3].toInt()
            val fps = parts[4].toInt()
            val bitrate = parts[5].toInt()
            val durationMilli = parts[6].toLong()
            val fileSize = parts[7].toLong()
            return KuRecordFile(
                fileId = fileId,
                dateTime = LocalDateTime.of(
                    ymd.substring(0, 4).toInt(),
                    ymd.substring(4, 6).toInt(),
                    ymd.substring(6, 8).toInt(),
                    hms.substring(0, 2).toInt(),
                    hms.substring(2, 4).toInt(),
                    hms.substring(4, 6).toInt()
                ),
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
