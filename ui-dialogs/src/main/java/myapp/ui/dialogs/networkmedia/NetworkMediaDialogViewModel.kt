package myapp.ui.dialogs.networkmedia

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.domain.interactors.SaveNetworkMedia
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal data class NetworkMediaViewState(
    val media: String = "off"  // wifi,lte
)


@HiltViewModel
internal class NetworkMediaDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val camManager: CamManager,
    private val saveNetworkMedia: SaveNetworkMedia,
) : ReduxViewModel<NetworkMediaViewState>(
    NetworkMediaViewState()
) {
    private val loadingState = ObservableLoadingCounter()

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()


    val wifiLive: LiveData<Boolean> =
        liveFieldOf(NetworkMediaViewState::media).map { it == "wifi" }
    val lteLive: LiveData<Boolean> =
        liveFieldOf(NetworkMediaViewState::media).map { it == "lte" }

    val wifiLteLive: LiveData<Boolean> =
        liveFieldOf(NetworkMediaViewState::media).map { it == "wifi,lte" }
    val offLive: LiveData<Boolean> =
        liveFieldOf(NetworkMediaViewState::media).map { it == "off" }

    init {
        viewModelScope.launch {
            camManager.observeConfig().collect { cfg ->
                if (cfg == null) {
                    setState { copy(media = "off") }
                } else {
                    setState { copy(media = cfg.enabledNetworkMedia) }
                }
            }
        }
    }


    fun updateMedia(media: String) {
        viewModelScope.launch {
            setState { copy(media = media) }
        }
    }

    fun updateMediaWifi() = updateMedia("wifi")
    fun updateMediaLte() = updateMedia("lte")
    fun updateMediaOff() = updateMedia("off")
    fun updateMediaWifiLte() = updateMedia("wifi,lte")


    suspend fun saveNetworkMedia(ip: String, media: String) {
        loadingState.addLoader()
        try {
            saveNetworkMedia.executeSync(ip = ip, networkMedia = media)
        } finally {
            loadingState.removeLoader()
        }
    }
}

