package myapp.ui.player.vlc

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Environment
import android.text.format.DateFormat
import android.util.Log
import splitties.init.appCtx
import java.io.*
import java.lang.Thread.UncaughtExceptionHandler

private const val TAG = "VLC/VlcCrashHandler"

class VLCCrashHandler : UncaughtExceptionHandler {

    private val defaultUEH: UncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        defaultUEH.uncaughtException(thread, saveLog(ex))
    }

    companion object {
        /**
         * Saves a [Throwable] stack trace in a crash file after having some useful info appended.
         * Also save a logcat file besides it
         * /!\ Will trigger the crash reporter in beta!
         * It's called by the [UncaughtExceptionHandler] but is useful in case we want to workaround a crash
         * but still get the trace in the crash reporter
         *
         * @param ex: the [Throwable] to log
         * @return the [Throwable] with versions appended
         */
        fun saveLog(ex: Throwable, watermark: String = ""): Throwable {
            val result = StringWriter()
            val printWriter = PrintWriter(result)

            // Inject some info about android version and the device, since google can't provide them in the developer console
            val trace = ex.stackTrace
            val trace2 = arrayOfNulls<StackTraceElement>(trace.size + if (watermark.isNotEmpty()) 4 else 3)
            System.arraycopy(trace, 0, trace2, 0, trace.size)
            trace2[trace.size + 0] = StackTraceElement("Android", "MODEL", android.os.Build.MODEL, -1)
            trace2[trace.size + 1] = StackTraceElement("Android", "VERSION", android.os.Build.VERSION.RELEASE, -1)
            trace2[trace.size + 2] = StackTraceElement("Android", "FINGERPRINT", android.os.Build.FINGERPRINT, -1)
            if (watermark.isNotEmpty()) trace2[trace.size + 3] = StackTraceElement("VLC", "Watermark", watermark, -1)
            ex.stackTrace = trace2

            ex.printStackTrace(printWriter)
            val stacktrace = result.toString()
            printWriter.close()
            Log.e(TAG, stacktrace)

            // Save the log on SD card if available
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                writeLog(stacktrace, appCtx.getExternalFilesDir(null)!!.absolutePath + "/vlc_crash")
                writeLogcat(appCtx.getExternalFilesDir(null)!!.absolutePath + "/vlc_logcat")
            }
            return ex
        }

        /**
         * Writes a log in a file
         *
         * @param log: the log string to write
         * @param name: the file name to write into
         */
        private fun writeLog(log: String, name: String) {
            val timestamp = DateFormat.format("yyyyMMdd_kkmmss", System.currentTimeMillis())
            val filename = name + "_" + timestamp + ".log"

            val stream: FileOutputStream
            try {
                stream = FileOutputStream(filename)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return
            }

            val output = OutputStreamWriter(stream)
            val bw = BufferedWriter(output)

            val version = try {
                val pInfo: PackageInfo = appCtx.packageManager.getPackageInfo(appCtx.packageName, 0)
                pInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            try {
                bw.write("App version: $version\r\n")
                bw.write(log)
                bw.newLine()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                bw.closeQuietly()
                output.closeQuietly()
            }
        }

        private fun Closeable.closeQuietly() {
            try {
                this.close()
            } catch (err: Throwable) {
            }
        }

        /**
         * Write the current log in a file
         *
         * @param name: the file name to use to save the logcat
         */
        private fun writeLogcat(name: String) {
//            val timestamp = DateFormat.format("yyyyMMdd_kkmmss", System.currentTimeMillis())
//            val filename = name + "_" + timestamp + ".log"
//            try {
//                Logcat.writeLogcat(filename)
//            } catch (e: IOException) {
//                Log.e(TAG, "Cannot write logcat to disk")
//            }

        }
    }
}
