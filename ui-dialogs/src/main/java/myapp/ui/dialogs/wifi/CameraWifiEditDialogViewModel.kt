package myapp.ui.dialogs.wifi

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.domain.interactors.UpdateWifi
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject

internal sealed class CameraWifiEditAction {
    data class SetSsid(val ssid: String?) : CameraWifiEditAction()
}

internal data class CameraWifiEditDialogState(
    val dummy: Long = 0,
)

@HiltViewModel
internal class CameraWifiEditDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logger: Logger,
    val camManager: CamManager,
    private val updateWifi: UpdateWifi
) : ReduxViewModel<CameraWifiEditDialogState>(CameraWifiEditDialogState()) {
    private val loadingState = ObservableLoadingCounter()
    private val pendingActions = Channel<CameraWifiEditAction>(Channel.BUFFERED)

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()

    // data binding
    val wifiSsidLive = MutableLiveData<String?>() // 양방향 바인딩이라서 MutableLiveData
    val wifiPwLive = MutableLiveData<String?>() // 양방향 바인딩이라서 MutableLiveData

    init {
        // 액션 처리
        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is CameraWifiEditAction.SetSsid -> {
                    }
                }
            }
        }
    }

    fun updateArgs(wifiSsid: String?, wifiPw: String?) {
        wifiSsidLive.value = wifiSsid
        wifiPwLive.value = wifiPw
    }

    suspend fun saveWifi(ip: String, wifiSsid: String, wifiPw: String) {
        loadingState.addLoader()
        try {
            updateWifi.executeSync(ip = ip, wifiSsid = wifiSsid, wifiPw = wifiPw)
        } finally {
            loadingState.removeLoader()
        }
    }
}

