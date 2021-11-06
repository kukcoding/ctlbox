package myapp.domain.interactors

import kotlinx.coroutines.delay
import myapp.error.AppErrorCode
import myapp.error.AppException

abstract class AbstractDownloader {
    protected var callback: ICallback? = null
    protected var mContentLength = 0L
    protected var mForceStop = false
    protected var mDownloadedLength = 0L

    private var mDownloading = false
    private var mCurrentSpeed: Int = 0 // byte per sec
    private var mStartTime = -1L
    private var mFinishTime = -1L
    private var mResultError: Exception? = null


    interface ICallback {
        suspend fun willStartDownload(downloader: AbstractDownloader)
        suspend fun updateProgress(downloader: AbstractDownloader)
        suspend fun didFinishDownload(downloader: AbstractDownloader, error: Exception?)
    }

    protected abstract fun downloadInternal()

    suspend fun download(callback: ICallback) {
        if (mDownloading) return
        mForceStop = false
        mStartTime = -1
        callback.willStartDownload(this)
        startDownloadThread(callback)
    }


    private suspend fun startDownloadThread(callback: ICallback) {
        mStartTime = System.currentTimeMillis()
        mFinishTime = -1
        mDownloadedLength = 0L
        mCurrentSpeed = 0
        mDownloading = true
        val t = Thread {
            try {
                downloadInternal()
            } catch (err: Exception) {
                mResultError = AppException.translate(err)
            } finally {
                mFinishTime = System.currentTimeMillis()
                mDownloading = false
            }
        }
        t.isDaemon = true
        t.start()

        while (mDownloading && !mForceStop) {
            val lastLen = mDownloadedLength
            delay(500)
            val downloadBytesInTick = mDownloadedLength - lastLen
            if (downloadBytesInTick >= 0) {
                mCurrentSpeed = (downloadBytesInTick / 0.5f).toInt()
            }
            callback.updateProgress(this)
        }

        if (mDownloadedLength > 0 && mContentLength == mDownloadedLength) {
            callback.didFinishDownload(this, null)
        } else {
            if (mForceStop) {
                if (mResultError == null) {
                    mResultError = AppException(AppErrorCode.E1_CANCEL)
                }
            }
            callback.didFinishDownload(this, mResultError)
        }

    }

    protected fun downloadFinishSuccessfully() {
        mDownloading = false
    }

    val isDownloading: Boolean
        get() {
            return this.mDownloading
        }


    val resultError: Exception?
        get() {
            return this.mResultError
        }

    val elapsedTime: Long
        get() {
            return if (mDownloading) {
                System.currentTimeMillis() - mStartTime
            } else {
                mFinishTime - mStartTime
            }
        }

    val downloadSpeedAverage: Int
        get() {
            if (mStartTime < 0) return 0
            val diff = System.currentTimeMillis() - mStartTime
            if (diff <= 0) return 0
            return ((mDownloadedLength * 1000f) / diff).toInt()
        }

    val downloadSpeedCurrent: Int
        get() {
            return mCurrentSpeed
        }

    val downloadedBytes: Long
        get() {
            return mDownloadedLength
        }


    val contentLength: Long
        get() {
            return mContentLength
        }
}
