package myapp.ui.dialogs.loginlte

import android.content.Context
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.domain.interactors.DoLogin
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal data class LoginLteDialogState(
    val dummy: Long = 0,
)

@HiltViewModel
internal class LoginLteDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logger: Logger,
    private val doLogin: DoLogin,
    private val camManager: CamManager
) : ReduxViewModel<LoginLteDialogState>(LoginLteDialogState()) {
    private val loadingState = ObservableLoadingCounter()

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()

    fun observeLogin(): Flow<Any> {
        return this.camManager.observeConfig().filterNotNull()
    }

    suspend fun tryLogin(ip: String, pw: String) {
        loadingState.addLoader()
        try {
            doLogin.executeSync(ip = ip, pw = pw)
        } finally {
            loadingState.removeLoader()
        }
    }
}

