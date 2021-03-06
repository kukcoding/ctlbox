package myapp.ui.home.leftmenu

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kr.ohlab.android.recyclerviewgroup.DefaultHolderTracker
import kr.ohlab.android.recyclerviewgroup.TRListAdapter
import kr.ohlab.android.recyclerviewgroup.support.buildListAdapter
import myapp.data.preferences.AppPreference
import myapp.data.preferences.KuCameraPreference
import myapp.extensions.confirmOk
import myapp.extensions.resources.resColor
import myapp.settings.BuildConstants
import myapp.ui.dialogs.CameraDialogs
import myapp.ui.home.HomeFragment
import myapp.ui.home.R
import myapp.ui.home.databinding.FragmentLeftMenuBinding
import myapp.ui.home.leftmenu.viewholder.LeftMenuCameraItem
import myapp.ui.home.leftmenu.viewholder.LeftMenuItem
import myapp.ui.home.leftmenu.viewholder.LeftMenuNaviItem
import myapp.ui.listeners.ContinuousClickCountListener
import myapp.util.AndroidUtils
import myapp.util.TRLayouts
import splitties.snackbar.snack
import javax.inject.Inject


@AndroidEntryPoint
class LeftMenuFragment : Fragment() {

    companion object {
        fun newInstance() = LeftMenuFragment()
    }

    @Inject
    lateinit var buildConstants: BuildConstants

//    @Inject
//    lateinit var activityLauncher: ActivityLauncher

    private var mAdapter: TRListAdapter? = null
    private val mViewModel: LeftMenuViewModel by viewModels()
    private lateinit var mBind: FragmentLeftMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mBind = FragmentLeftMenuBinding.inflate(inflater, container, false)
        mBind.vm = mViewModel
        mBind.lifecycleOwner = this
        return mBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customInit()
        setupEvents()
    }

    private fun customInit() {
        TRLayouts.height(AndroidUtils.getStatusBarHeight(), mBind.dummyStatusBar)
        setupRecyclerView()
    }

    private fun setupEvents() {
        mViewModel.viewItems.observe(viewLifecycleOwner, {
            mAdapter?.submitList(it)
        })

        mBind.txtviewAppVersion.setOnClickListener(object : ContinuousClickCountListener() {
            override fun onClickCountChanged(count: Int): Boolean {
                if (count == 10) {
                    if (AppPreference.isAdmin.value) {
                        AppPreference.isAdmin.value = false
                        mBind.root.snack("Admin mode canceled")
                    } else {
                        AppPreference.isAdmin.value = true
                        mBind.root.snack("Admin mode entered")
                    }
                    return true
                }
                return false
            }
        })

        mBind.txtviewAddCameraBtn.setOnClickListener {
            openLoginNetworkChoose(lastCameraIp = null)
        }

        AppPreference.isAdmin.valueFlow().distinctUntilChanged().asLiveData().observe(viewLifecycleOwner, { admin ->
            if (admin) {
                mBind.txtviewAppVersion.setTextColor(resColor(R.color.color_orange_500))
            } else {
                mBind.txtviewAppVersion.setTextColor(resColor(R.color.colorDarkText7))
            }
        })

        mViewModel.liveFieldOf(LeftMenuViewState::cameraId).distinctUntilChanged()
            .observe(viewLifecycleOwner, { cameraId ->
                mAdapter?.notifyDataSetChanged()
            })
    }

    private fun setupRecyclerView() {
        mAdapter = buildListAdapter(mBind.recyclerView, spanCount = 1) {
            addViewType(LeftMenuNaviItem::class)
            addViewType(LeftMenuItem::class)
            addViewType(LeftMenuCameraItem::class)
        }

        mAdapter!!.apply {

            // ?????? ?????? ?????? ?????????
            registerOnBindBefore(LeftMenuNaviItem::class) { item ->
                //item.onMarketReviewClick = ::onHolderClickNaviMarketReview
                //item.onAppShareClick = ::onHolderClickNaviAppShare
                // item.onSettingClick = ::onHolderClickNaviSetting
                item.onWifiClickClick = ::onHolderClickNaviWifiClick
            }

            // ?????? ?????? ?????????
            registerOnBindBefore(LeftMenuItem::class) { item ->
                item.onHolderClick = ::onHolderClickMenu
            }

            // ????????? ?????????
            registerOnBindBefore(LeftMenuCameraItem::class) { item ->
                item.onHolderClick = ::onHolderClickCamera
                item.onDeleteClick = ::onHolderClickCameraDelete
                item.onDisconnectClick = ::onHolderClickCameraDisconnect
            }
        }


        mAdapter!!.holderTracker = object : DefaultHolderTracker() {
            override fun holderSelected(item: Any): Boolean {
                val cameraId = (item as? LeftMenuCameraItem)?.camera?.cameraId ?: return false
                val state = mViewModel.currentState()
                return cameraId == state.cameraId
            }
        }
    }


    /**
     * ????????? ?????? - ?????? ?????????
     */
    private fun onHolderClickMenu(view: View, item: LeftMenuItem) {
        when (item.menu) {
            LeftMenu.DUMMY -> {
//                if (!NetworkUtils.hasInternet(requireContext())) {
//                    alert("???????????? ????????? ??????????????????")
//                    return
//                }
//                activityLauncher.startJebo(this)
            }
            else -> {
                mBind.root.snack("?????? ??????")
            }
        }
    }

    /**
     * ????????? ?????? - ?????????
     */
    private fun onHolderClickCamera(view: View, item: LeftMenuCameraItem) {
        val cameraId = mViewModel.camManager.cameraId
        if (cameraId != null && cameraId == item.camera.cameraId) {
            mBind.root.snack("?????? ????????? ??????????????????")
            return
        }

        val camera = item.camera
        openLoginNetworkChoose(lastCameraIp = camera.lastIp)
    }

    private fun openLoginNetworkChoose(lastCameraIp: String?) {
        CameraDialogs.openLoginNetworkChoose(fm = childFragmentManager) { net ->
            if (net == "wifi") {
                CameraDialogs.openLoginWifi(fm = childFragmentManager) { loggedIn ->
                    if (loggedIn) {
                        closeDrawer()
                    }
                }

            } else if (net == "lte") {
                CameraDialogs.openLoginLte(fm = childFragmentManager, cameraIp = lastCameraIp) { loggedIn ->
                    if (loggedIn) {
                        closeDrawer()
                    }
                }
            }
        }
    }

    private fun closeDrawer() {
        (parentFragment as? HomeFragment)?.closeDrawer()
    }


    /**
     * ????????? ?????? - ????????? ??????
     */
    private fun onHolderClickCameraDelete(view: View, item: LeftMenuCameraItem) {

        confirmOk("????????? ???????????? ?????????????????????????", item.camera.cameraName ?: item.camera.cameraId) { ok ->
            if (ok) {
                val rootView = item.viewHolder?.binding?.root ?: return@confirmOk
                YoYo.with(Techniques.FadeOutLeft)
                    .duration(300)
                    .onEnd {
                        KuCameraPreference.remove(item.camera.cameraId)
                    }.playOn(rootView)
            }
        }
    }


    /**
     * ????????? ?????? - ????????? ?????? ??????
     */
    private fun onHolderClickCameraDisconnect(view: View, item: LeftMenuCameraItem) {
        mViewModel.submitAction(LeftMenuAction.DisconnectCurrentCam)
    }

    /**
     * ????????? ?????? - ???????????? ?????? ??????
     */
    private fun onHolderClickNaviWifiClick(view: View, item: LeftMenuNaviItem) {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    /**
     * ????????? ?????? - ??? ??????
     */
    private fun onHolderClickNaviAppShare(view: View, item: LeftMenuNaviItem) {
        val ctx = activity ?: return
        try {
            val shareIntent = ShareCompat.IntentBuilder.from(ctx).setType("text/plain")
                .setChooserTitle(getString(R.string.app_name))
                .setText(buildConstants.playStoreUrl)
                .intent
            if (shareIntent.resolveActivity(ctx.packageManager) != null) {
                startActivity(shareIntent)
            }
        } catch (e: ActivityNotFoundException) {
            // emulator????????? ??? ????????? ?????????
        }
    }

}
