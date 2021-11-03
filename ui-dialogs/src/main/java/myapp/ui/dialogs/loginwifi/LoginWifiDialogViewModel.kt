package myapp.ui.dialogs.loginwifi

import android.content.Context
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import myapp.BuildVars
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.domain.interactors.DoLogin
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal data class LoginWifiDialogState(
    val dummy: Long = 0,
)

@HiltViewModel
internal class LoginWifiDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logger: Logger,
    private val doLogin: DoLogin,
    private val camManager: CamManager
) : ReduxViewModel<LoginWifiDialogState>(LoginWifiDialogState()) {
    private val loadingState = ObservableLoadingCounter()

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()

    fun observeLogin(): Flow<Any> {
        return this.camManager.observeConfig().filterNotNull()
    }

    suspend fun tryLogin(pw: String) {
        loadingState.addLoader()
        try {
            doLogin.executeSync(ip = BuildVars.cameraAccessPointIp, pw = pw)
        } finally {
            loadingState.removeLoader()
        }
    }
}

