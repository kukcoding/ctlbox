package myapp.domain.interactors

import androidx.annotation.WorkerThread
import myapp.extensions.closeQuietly
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import java.io.File

class RecordFileDownloader(
    private val url: HttpUrl,
    private val destinationFile: File,
) : AbstractDownloader() {

    private var mResponse: Response? = null

    @WorkerThread
    override fun downloadInternal() {
        var success = false

        try {
            downloadFile(destinationFile)
            success = true
        } finally {
            if (!success) {
                if (destinationFile.exists()) {
                    Timber.i("다운로드 실패, 파일삭제함: ${destinationFile.absolutePath}")
                    destinationFile.delete()
                }
            }
        }
    }

    @WorkerThread
    private fun downloadFile(tempFile: File): Long {
        val client = OkHttpClient.Builder().build()

        try {
            val request = Request.Builder().url(url)
                .addHeader("Cache-Control", "no-cache")
                .build()
            val response = client.newCall(request).execute()
            mResponse = response
            val fileSize = response.body!!.contentLength()
            if (fileSize != -1L) {
                this.mContentLength = fileSize
            }

            var totalDownloaded = 0L
            tempFile.outputStream().use { outputStream ->
                response.body!!.byteStream().use { inputStream ->
                    val buffer = ByteArray(4096)
                    do {
                        val nbytes = inputStream.read(buffer)
                        if (nbytes > 0) {
                            outputStream.write(buffer, 0, nbytes)
                            totalDownloaded += nbytes
                            mDownloadedLength = totalDownloaded
                        }
                    } while (nbytes > 0)
                }
            }
            downloadFinishSuccessfully()
            if (!mForceStop && totalDownloaded > 0) {
                return totalDownloaded
            }
        } finally {
            clean()
        }
        return -1L
    }


    private fun clean() {
        mResponse?.closeQuietly()
        mResponse = null
    }

}
