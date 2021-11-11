package myapp.domain.interactors

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import myapp.BuildVars
import myapp.base.mediastore.MediaStoreHelper
import myapp.error.AppException
import myapp.util.Logger
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File
import javax.inject.Inject

sealed class DownloadStatus {
    object First : DownloadStatus()
    object Downloading : DownloadStatus()
    data class Saving(
        val elapsedTime: Long,
        val downloadedBytes: Long,
        val contentLength: Long,
        val speed: Int,
        val speedAvg: Int
    ) : DownloadStatus()

    object CopyFile : DownloadStatus()
    object TempFileDelete : DownloadStatus()
    data class Success(val savedFileName: String) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}

class DownloadRecordFile @Inject constructor(
    @ApplicationContext val context: Context,
    private val logger: Logger,
) {

    operator fun invoke(downloadUrl: String, destFileName: String, destFolderName: String): Flow<DownloadStatus> {
        val url = if (BuildVars.fakeCamera) {
            "https://ohlab.kr/p/kuk/sample/stevejobs.mp4".toHttpUrl()
        } else {
            downloadUrl.toHttpUrl()
        }
        return start(
            downloadUrl = url,
            destFileName = destFileName,
            destFolderName = destFolderName
        ).flowOn(Dispatchers.IO).catch { t ->
            t.printStackTrace()
            if (t is AppException) {
                emit(DownloadStatus.Error(t.customMessage ?: t.message))
            } else {
                emit(DownloadStatus.Error(t.message ?: "에러가 발생했습니다"))
            }
        }.onCompletion {
            clearTempDir()
        }
    }

    private fun prepareTempDir(): File {
        clearTempDir()
        return File(context.cacheDir, "download").also { it.mkdirs() }
    }

    private fun clearTempDir() {
        val downloadDir = File(context.cacheDir, "download")
        if (downloadDir.exists()) downloadDir.deleteRecursively()
    }

    private fun start(
        downloadUrl: HttpUrl,
        destFileName: String,
        destFolderName: String
    ): Flow<DownloadStatus> = flow {
        val downloadDir = prepareTempDir()

        // 다운로드
        emit(DownloadStatus.Downloading)
        val tmpFile = download(this, downloadUrl = downloadUrl, tmpFile = File(downloadDir, "downloading.recordfile"))
            ?: return@flow

        // 파일 복사
        emit(DownloadStatus.CopyFile)
        copyFile(tmpFile, destFileName, destFolderName)

        // 임시 파일 삭제
        emit(DownloadStatus.TempFileDelete)
        clearTempDir()

        // 완료
        emit(DownloadStatus.Success(destFileName))
    }

    private fun copyFile(tmpFile: File, fileName: String, albumName: String) {
        MediaStoreHelper.copyVideoToGallery(
            videoFile = tmpFile,
            fileName = fileName,
            albumName = albumName
        )
    }

    private suspend fun download(collector: FlowCollector<DownloadStatus>, downloadUrl: HttpUrl, tmpFile: File): File? {
        val deferred = CompletableDeferred<File?>()
        val fileDownloader = RecordFileDownloader(downloadUrl, tmpFile)
        fileDownloader.download(object : AbstractDownloader.ICallback {
            override suspend fun willStartDownload(downloader: AbstractDownloader) {
                collector.emit(DownloadStatus.Downloading)
            }

            override suspend fun updateProgress(downloader: AbstractDownloader) {
                collector.emit(
                    DownloadStatus.Saving(
                        elapsedTime = downloader.elapsedTime,
                        downloadedBytes = downloader.downloadedBytes,
                        contentLength = downloader.contentLength,
                        speed = downloader.downloadSpeedCurrent,
                        speedAvg = downloader.downloadSpeedAverage,
                    )
                )
            }

            override suspend fun didFinishDownload(downloader: AbstractDownloader, error: Exception?) {
                if (error != null) {
                    val msg = if (error is AppException) {
                        error.displayMessage()
                    } else {
                        error.message
                    }
                    collector.emit(DownloadStatus.Error(msg ?: "unknown error"))
                    deferred.complete(null)
                } else {
                    deferred.complete(tmpFile)
                }
            }
        })

        return deferred.await()
    }

}
