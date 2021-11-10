package myapp.ui.dialogs.recordfiledownload

import android.content.Context
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import myapp.Cam
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.data.entities.KuRecordFile
import myapp.domain.interactors.DownloadRecordFile
import myapp.domain.interactors.DownloadStatus
import myapp.extensions.humanReadableSize
import myapp.flowInterval
import myapp.ui.dialogs.R
import myapp.util.tupleOf
import javax.inject.Inject
import kotlin.math.roundToInt

internal data class DownloadSpeed(
    val elapsedTime: Long,
    val downloadedBytes: Long,
    val contentLength: Long,
    val speed: Int,
    val speedAvg: Int
)

internal sealed class RecordFileDownloadAction {
    object AskCancel : RecordFileDownloadAction()
    data class UpdateCancel(val canceled: Boolean) : RecordFileDownloadAction()
}

internal data class RecordFileDownloadViewState(
    val startTime: Long? = null,
    val finishTime: Long? = null,
    val downloadSpeed: DownloadSpeed? = null,
    val downloadStatus: DownloadStatus = DownloadStatus.First,
    val errorMessage: String? = null,
    val downloadedFileName: String? = null,
    val recordFile: KuRecordFile? = null,
    val fileName: String? = null,
    val cameraName: String? = null,
    val cancelRequested: Boolean = false,
    val canceled: Boolean = false
)

private fun secondText(from: Long, to: Long): String {
    return ((to - from) / 1000f).roundToInt().toString() +"초"
}

@HiltViewModel
internal class RecordFileDownloadViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val camManager: CamManager,
    private val downloadRecordFile: DownloadRecordFile,
) : ReduxViewModel<RecordFileDownloadViewState>(
    RecordFileDownloadViewState()
) {
    private val pendingActions = Channel<RecordFileDownloadAction>(Channel.BUFFERED)
    private val tickFlow = flowInterval(500, 500)

    // for data binding
    val fileName1Live = liveFieldOf(RecordFileDownloadViewState::recordFile).map { recordFile ->
        if (recordFile == null) {
            ""
        } else {
            // 1920x1080 / 30fps (40.5초)
            val duration = String.format("%d", recordFile.durationMilli / 1000)
            "${recordFile.width}x${recordFile.height} / ${recordFile.fps}fps (${duration}초)"
        }
    }

    val fileName2Live = liveFieldOf(RecordFileDownloadViewState::recordFile).map { recordFile ->
        recordFile?.galleryFileName ?: ""
    }

    val cancelConfirmVisibleLive = combine(
        flowFieldOf(RecordFileDownloadViewState::downloadStatus),
        flowFieldOf(RecordFileDownloadViewState::cancelRequested),
        ::tupleOf
    ).map { (status, cancelRequested) ->
        if (status is DownloadStatus.Success || status is DownloadStatus.Error) {
            false
        } else {
            cancelRequested
        }
    }.distinctUntilChanged().asLiveData()

    val elapsedTimeTextLive = combine(
        flowFieldOf(RecordFileDownloadViewState::startTime),
        flowFieldOf(RecordFileDownloadViewState::finishTime),
        tickFlow,
        ::tupleOf
    ).map { (startTime, finishTime, _) ->
        when {
            startTime == null -> ""
            finishTime == null -> secondText(startTime, System.currentTimeMillis())
            else -> secondText(startTime, finishTime)
        }
    }.distinctUntilChanged().asLiveData()

    // for data binding
    val finishButtonText = liveFieldOf(RecordFileDownloadViewState::downloadStatus).map {
        when (it) {
            is DownloadStatus.Success -> "닫기"
            is DownloadStatus.Error -> "닫기"
            else -> "취소"
        }
    }

    // for data binding
    val isFinished = liveFieldOf(RecordFileDownloadViewState::downloadStatus).map {
        it is DownloadStatus.Success || it is DownloadStatus.Error
    }

    // for data binding
    val errorMessage = combine(
        flowFieldOf(RecordFileDownloadViewState::errorMessage),
        flowFieldOf(RecordFileDownloadViewState::canceled),
        ::tupleOf
    ).map { (errMsg, canceled) ->
        if (canceled) "취소되었습니다" else errMsg
    }.asLiveData()

    val extraMsgLive = liveFieldOf(RecordFileDownloadViewState::downloadSpeed).map {
        if (it == null) {
            ""
        } else {
            "${humanReadableSize(it.speed)} / 초"
        }
    }

    val fileSizeTextLive = combine(
        flowFieldOf(RecordFileDownloadViewState::downloadStatus),
        flowFieldOf(RecordFileDownloadViewState::downloadSpeed),
        ::tupleOf
    ).map { (status, speed) ->
        when {
            speed == null -> ""
            status is DownloadStatus.Saving || status is DownloadStatus.Error -> {
                "${humanReadableSize(speed.downloadedBytes)} / ${humanReadableSize(speed.contentLength)}"
            }
            else -> {
                humanReadableSize(speed.contentLength)
            }
        }
    }.asLiveData()


    // for data binding
    val statusText = combine(
        flowFieldOf(RecordFileDownloadViewState::downloadStatus),
        flowFieldOf(RecordFileDownloadViewState::canceled),
        ::tupleOf
    ).map { (status, canceled) ->
        if (canceled) {
            "" // 취소되었을때는 에러메시지만 표시하므로 상태메시지는 없앤다
        } else {
            when (status) {
                is DownloadStatus.First -> context.getString(R.string.msg_preparing)
                is DownloadStatus.Downloading -> context.getString(R.string.msg_downloading)
                is DownloadStatus.Saving -> context.getString(R.string.msg_downloading)
                is DownloadStatus.TempFileDelete -> context.getString(R.string.msg_temp_file_deleting)
                is DownloadStatus.CopyFile -> context.getString(R.string.msg_file_copying)
                is DownloadStatus.Success -> context.getString(R.string.msg_saved)
                is DownloadStatus.Error -> "다운로드 실패"
                else -> ""
            }
        }
    }.asLiveData()


    private var supervisor: CompletableJob? = null

    init {
        // 액션 처리
        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is RecordFileDownloadAction.AskCancel -> setState { copy(cancelRequested = true) }
                    is RecordFileDownloadAction.UpdateCancel -> {
                        if (action.canceled) {
                            cancelIfNotFinished()
                        } else {
                            setState { copy(cancelRequested = false) }
                        }
                    }
                }
            }
        }
    }

    fun startDownload() {
        val recordFile = this.currentState().recordFile ?: return
        val folderName = camManager.config?.cameraName ?: return
        val cameraIp = camManager.cameraIp ?: return
        val fileName = recordFile.galleryFileName

        val downloadUrl = Cam.recordFileUrl(ip = cameraIp, fileId = recordFile.fileId)
        supervisor = SupervisorJob()
        viewModelScope.launch(supervisor!!) {
            setState { copy(startTime = System.currentTimeMillis()) }
            downloadRecordFile.invoke(
                downloadUrl = downloadUrl,
                destFileName = fileName,
                destFolderName = folderName
            ).collectStatus()
        }
    }

    private suspend fun Flow<DownloadStatus>.collectStatus() = collect { newState ->
        when (newState) {
            is DownloadStatus.Saving -> setState {
                copy(
                    downloadStatus = newState,
                    downloadSpeed = DownloadSpeed(
                        elapsedTime = newState.elapsedTime,
                        downloadedBytes = newState.downloadedBytes,
                        contentLength = newState.contentLength,
                        speed = newState.speed,
                        speedAvg = newState.speedAvg
                    )
                )
            }

            is DownloadStatus.Error -> setState {
                copy(
                    downloadStatus = newState,
                    errorMessage = newState.message,
                    finishTime = System.currentTimeMillis(),
                    cancelRequested = false
                )
            }

            is DownloadStatus.Success -> setState {
                copy(
                    downloadStatus = newState,
                    downloadedFileName = newState.savedFileName,
                    finishTime = System.currentTimeMillis(),
                    cancelRequested = false,
                    canceled = false
                )
            }

            else -> setState { copy(downloadStatus = newState) }
        }
    }

    fun submitAction(vararg actions: RecordFileDownloadAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                actions.forEach { pendingActions.send(it) }
            }
        }
    }


    private suspend fun cancelIfNotFinished() {
        val status = currentState().downloadStatus
        supervisor?.cancel()
        supervisor = null
        if (status is DownloadStatus.Success && status is DownloadStatus.Error) {
            setState {
                copy(cancelRequested = false, canceled = false)
            }
        } else {
            setState {
                copy(
                    cancelRequested = false,
                    canceled = true,
                    finishTime = System.currentTimeMillis(),
                    downloadStatus = DownloadStatus.Error("Canceled"),
                )
            }
        }
    }

    fun updateArgs(cameraName: String, fileId: String) {
        viewModelScope.launch {
            setState {
                copy(
                    cameraName = cameraName,
                    fileName = fileId,
                    recordFile = KuRecordFile.createFromFileId(fileId)
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        supervisor?.cancel()
    }
}
