package myapp.ui.dialogs.camerapassword

import android.content.Context
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.domain.interactors.SaveCameraPassword
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal data class CameraPasswordEditDialogState(
    val dummy: Long = 0,
)

@HiltViewModel
internal class CameraPasswordEditDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logger: Logger,
    val camManager: CamManager,
    private val saveCameraPassword: SaveCameraPassword,
) : ReduxViewModel<CameraPasswordEditDialogState>(CameraPasswordEditDialogState()) {
    private val loadingState = ObservableLoadingCounter()

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()


    suspend fun doSaveCameraPw(ip: String, pw: String) {
        loadingState.addLoader()
        return try {
            saveCameraPassword.executeSync(ip = ip, pw = pw)
        } finally {
            loadingState.removeLoader()
        }
    }
}

