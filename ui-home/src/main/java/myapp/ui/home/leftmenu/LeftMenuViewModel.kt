package myapp.ui.home.leftmenu

import android.content.Context
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.ohlab.android.recyclerviewgroup.ItemBase
import myapp.ReduxViewModel
import myapp.data.cam.CamManager
import myapp.data.preferences.AppPreference
import myapp.data.preferences.KuCameraPreference
import myapp.ui.home.leftmenu.viewholder.LeftMenuCameraItem
import myapp.ui.home.leftmenu.viewholder.LeftMenuItem
import myapp.ui.home.leftmenu.viewholder.LeftMenuNaviItem
import myapp.util.BaseUtils
import myapp.util.Logger
import myapp.util.tupleOf
import javax.inject.Inject

internal sealed class LeftMenuAction {
    object DisconnectCurrentCam : LeftMenuAction()
}

internal data class LeftMenuViewState(
    val naviItem: LeftMenuNaviItem = LeftMenuNaviItem(),
    val cameraItems: List<LeftMenuCameraItem> = emptyList(),
    val menuItems: List<LeftMenuItem> = emptyList(),
    val cameraId: String? = null
)

@HiltViewModel
internal class LeftMenuViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val camManager: CamManager,
    private val logger: Logger
) : ReduxViewModel<LeftMenuViewState>(LeftMenuViewState()) {

    private val pendingActions = Channel<LeftMenuAction>(Channel.BUFFERED)

    val isEmptyLive = liveFieldOf(LeftMenuViewState::cameraItems).map { it.isEmpty() }

    val viewItems = combine(
        flowFieldOf(LeftMenuViewState::naviItem),
        flowFieldOf(LeftMenuViewState::cameraItems),
        flowFieldOf(LeftMenuViewState::menuItems),
        ::tupleOf
    ).debounce(100).map { (navi, cameras, menuItems) ->
        mutableListOf<ItemBase<*>>().apply {
            add(navi)
            addAll(cameras)
            addAll(menuItems)
        }
    }.asLiveData()

    // for data binding
    val appVersionLive = AppPreference.isAdmin.valueFlow().map { admin ->
        if (admin) "ADMIN"
        else BaseUtils.appVersionName(context)
    }.asLiveData()


    init {

        viewModelScope.launch {
            KuCameraPreference.observeCameraList().collect { cameras ->
                setState {
                    copy(
                        cameraItems = cameras.map { LeftMenuCameraItem(it) }
                    )
                }
            }
        }

        viewModelScope.launch {
            camManager.observeConfig().map { it?.cameraId }.distinctUntilChanged().collect {
                setState { copy(cameraId = it) }
            }
        }
        // 액션 처리
        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is LeftMenuAction.DisconnectCurrentCam -> {
                        camManager.onDisconnect()
                    }
                }
            }
        }
    }

    fun submitAction(action: LeftMenuAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                pendingActions.send(action)
            }
        }
    }

}

