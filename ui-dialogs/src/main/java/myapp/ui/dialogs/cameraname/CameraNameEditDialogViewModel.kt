package myapp.ui.dialogs.cameraname

import android.content.Context
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.domain.interactors.SaveCameraName
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal data class CameraNameEditDialogState(
    val dummy: Long = 0,
)

@HiltViewModel
internal class CameraNameEditDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logger: Logger,
    val camManager: CamManager,
    private val saveCameraName: SaveCameraName,
) : ReduxViewModel<CameraNameEditDialogState>(CameraNameEditDialogState()) {
    private val loadingState = ObservableLoadingCounter()

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()

    suspend fun doSaveCameraName(ip: String, cameraName: String) {
        loadingState.addLoader()
        return try {
            saveCameraName.executeSync(ip = ip, cameraName = cameraName)
        } finally {
            loadingState.removeLoader()
        }
    }
}

