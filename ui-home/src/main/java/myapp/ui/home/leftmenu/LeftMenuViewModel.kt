package myapp.ui.home.leftmenu

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
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

    // 기존 광고를 제거하고, 새로운 광고를 로드한다
    object ClearAndRefreshFullscreenAd : LeftMenuAction()
}

internal data class LeftMenuViewState(
    val naviItem: LeftMenuNaviItem = LeftMenuNaviItem(),
    val cameraItems: List<LeftMenuCameraItem> = emptyList(),
    val menuItems: List<LeftMenuItem> = emptyList(),
)

@HiltViewModel
internal class LeftMenuViewModel @Inject constructor(
    @ApplicationContext context: Context,
//    private val observeLocalBibles: ObserveLocalBibles,
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

        // 액션 처리
        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is LeftMenuAction.ClearAndRefreshFullscreenAd -> clearAndRefreshFullscreenAd()
                }
            }
        }
    }

    fun setupWithLifecycle(lifecycleScope: LifecycleCoroutineScope) {
//        lifecycleScope.launch {
//            setState {
//                // val menuItems = LeftMenu.values().map { LeftMenuItem(menu = it, showDivider = true) }
//
//                copy(
//                    // menuItems = menuItems,
//                    cameraItems = listOf(
//                        KuCamera(
//                            cameraId = "12AB11CA",
//                            cameraName = "거실 카메라",
//                            lastConnectTimestamp = System.currentTimeMillis()
//                        ),
//                        KuCamera(
//                            cameraId = "AB00F0CA",
//                            cameraName = "안방 카메라",
//                            lastConnectTimestamp = System.currentTimeMillis()
//                        )
//                    ).map { LeftMenuCameraItem(it) }
//                )
//            }
//        }
    }

    fun submitAction(action: LeftMenuAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                pendingActions.send(action)
            }
        }
    }

    private fun clearAndRefreshFullscreenAd() {
//        viewModelScope.launch {
//            adManager.refreshFullscreenAd()
//        }
    }
}

