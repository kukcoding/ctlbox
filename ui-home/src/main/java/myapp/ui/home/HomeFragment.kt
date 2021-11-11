package myapp.ui.home


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import myapp.BuildVars
import myapp.Cam
import myapp.data.cam.CamLoggedIn
import myapp.data.cam.CamLoggedOut
import myapp.extensions.resources.resColor
import myapp.extensions.resources.resStr
import myapp.extensions.resources.styledColor
import myapp.ui.TwiceBackPressedCallback
import myapp.ui.dialogs.CameraDialogs
import myapp.ui.home.databinding.FragmentHomeBinding
import myapp.ui.home.leftmenu.LeftMenuFragment
import myapp.ui.player.LivePlayerActivity
import myapp.ui.record.RecordFilesActivity
import myapp.ui.settings.SettingsActivity
import myapp.util.AndroidUtils
import org.threeten.bp.Instant
import splitties.snackbar.snack


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val mViewModel: HomeViewModel by viewModels()
    private lateinit var mBind: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = FragmentHomeBinding.inflate(inflater, container, false)
        mBind.lifecycleOwner = this
        mBind.vm = mViewModel
        return mBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customInit()
        setupEvents()

        // 백키핸들러 등록, call reverse order, (FIRST-IN-LAST-CALLED)
        listOf(
            twiceBackPressedCallback,
            sideMenuCloseBackPressedCallback
        ).forEach {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it)
        }
    }

    private fun customInit() {
        setupDrawerLayout()
        if (childFragmentManager.findFragmentByTag(LeftMenuFragment::class.simpleName) == null) {
            childFragmentManager.let { fm ->
                fm.commit(true) {
                    add(mBind.layoutLeftMenuContainer.id, LeftMenuFragment(), LeftMenuFragment::class.simpleName)
                }
            }
        }

        if (childFragmentManager.findFragmentByTag(MjpgPlayerFragment::class.simpleName) == null) {
            childFragmentManager.let { fm ->
                fm.commit(true) {
                    add(mBind.layoutPlayerContainer.id, MjpgPlayerFragment(), MjpgPlayerFragment::class.simpleName)
                }
            }
        }
    }

    private fun setupEvents() {

        mBind.toolbar.setNavigationOnClickListener { toggleLeftMenu() }

        mBind.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                sideMenuCloseBackPressedCallback.isEnabled = true
            }

            override fun onDrawerClosed(drawerView: View) {
                sideMenuCloseBackPressedCallback.isEnabled = false
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })

        mBind.layoutLoginBtn.setOnClickListener { onClickLoginButton() }
        mBind.layoutNetworkBtn.setOnClickListener { openWifiSetting() }

        // RTSP 플레이 버튼 클릭
        mBind.btRtspPlayButton.setOnClickListener {
            val cameraIp = checkCameraIpOrNull()
            if (cameraIp != null) {
                if (BuildVars.fakeCamera) {
                    startActivity(
                        LivePlayerActivity.createIntent(
                            requireContext(),
                            uri = Uri.parse("https://ohlab.kr/p/kuk/sample/stevejobs.mp4")
                        )
                    )
                } else {
                    startActivity(
                        LivePlayerActivity.createIntent(requireContext(), Uri.parse(Cam.rtspUrl(cameraIp)))
                    )
                }
            }
        }

        // 녹화파일 목록 버튼 클릭
        mBind.btRecordList.setOnClickListener {
            if (checkCameraIpOrNull() != null) {
                startActivity(RecordFilesActivity.createIntent(requireContext()))
            }
        }

        // 카메라 설정 버튼 클릭
        mBind.btSetting.setOnClickListener {
            if (checkCameraIpOrNull() != null) {
                startActivity(SettingsActivity.createIntent(requireContext()))
            }
        }


        mViewModel.isLteEnabledLive.observe(viewLifecycleOwner, { enabled ->
            if (enabled == true) {
                mBind.txtviewLteLabel.alpha = 1f;
                mBind.txtviewLteLabel.setTextColor(resColor(R.color.color_green_500))
            } else {
                mBind.txtviewLteLabel.alpha = 0.3f
                mBind.txtviewLteLabel.setTextColor(styledColor(R.attr.colorOnSurface3))
            }
        })

        mViewModel.isWifiEnabledLive.observe(viewLifecycleOwner, { enabled ->
            if (enabled == true) {
                mBind.txtviewWifiLabel.alpha = 1f
                mBind.txtviewWifiLabel.setTextColor(resColor(R.color.color_green_500))
            } else {
                mBind.txtviewWifiLabel.alpha = 0.3f
                mBind.txtviewWifiLabel.setTextColor(styledColor(R.attr.colorOnSurface3))
            }
        })

        mViewModel.isRecordingLive.observe(viewLifecycleOwner, { recording ->
            if (recording) {
                mBind.txtviewRecordingLabel.setTextColor(resColor(R.color.color_green_500))
            } else {
                mBind.txtviewRecordingLabel.setTextColor(styledColor(R.attr.colorOnSurface3))
            }
        })


        mViewModel.liveFieldOf(HomeState::loginState).observe(viewLifecycleOwner, { state ->
            when (state) {
                is CamLoggedOut -> mBind.layoutNetworkBtn.isVisible = false
                is CamLoggedIn -> {
                    if (state.cameraIp == BuildVars.cameraAccessPointIp) {
                        mBind.loginNetworkLabel.title = "WIFI"
                    } else {
                        mBind.loginNetworkLabel.title = "LTE"
                    }
                    mBind.layoutNetworkBtn.isVisible = true
                }
            }
        })

        mBind.txtviewRecordingLabel.setOnClickListener {
            openRecordingSchedule()
        }

        mBind.txtviewWifiLabel.setOnClickListener {
            openNetworkMediaSetting()
        }
        mBind.txtviewLteLabel.setOnClickListener {
            openNetworkMediaSetting()
        }
    }

    fun closeDrawer() {
        mBind.drawerLayout.closeDrawer(Gravity.LEFT)
    }

    private fun checkCameraIpOrNull(): String? {
        if (!mViewModel.camManager.isLoggedIn) {
            mBind.root.snack("카메라에 연결되지 않았습니다")
            return null
        }
        return mViewModel.camManager.cameraIp
    }

    private fun setupDrawerLayout() {
        val ctx = requireContext()
        val isSmallTablet = AndroidUtils.isSmallTablet(ctx)
        val isLargeTablet = AndroidUtils.isLargeTablet(ctx)
        val isLandscape = AndroidUtils.isLandscape(ctx)
        val screenWidth = AndroidUtils.screenWidth(ctx)
        val navWidth = when {
            isLargeTablet -> {
                if (isLandscape) {
                    (screenWidth * 0.4).toInt()
                } else {
                    (screenWidth * 0.6).toInt()
                }
            }
            isSmallTablet -> {
                if (isLandscape) {
                    (screenWidth * 0.5).toInt()
                } else {
                    (screenWidth * 0.7).toInt()
                }
            }
            else -> {
                if (isLandscape) {
                    (screenWidth * 0.6).toInt()
                } else {
                    (screenWidth * 0.87).toInt()
                }
            }
        }
        mBind.navLeft.updateLayoutParams { width = navWidth }
    }

    private fun toggleLeftMenu() {
        if (mBind.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mBind.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            mBind.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun openWifiSetting() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    private fun onClickLoginButton() {
        val ctx = context ?: return
        if (mViewModel.isLoggedInLive.value == true) {
            mViewModel.submitAction(HomeAction.Logout)
            return
        }

        openLoginNetworkChoose()

//        val popup = PopupMenu(ContextThemeWrapper(context, R.style.LoginPopupMenu), mBind.txtviewLoginBtn)
//        popup.inflate(R.menu.popup_login_media)
//
//        //val initial = mViewModel.currentState().playerAutoStopTime
//        // popup.menu.iterator().forEach { }
//
//        popup.setOnMenuItemClickListener { menuItem ->
//            if (menuItem.itemId == R.id.popup_wifi) {
//                CameraDialogs.openLoginWifi(fm = childFragmentManager)
//            } else {
//                CameraDialogs.openLoginLte(fm = childFragmentManager, cameraIp = null)
//            }
//            true
//        }
//        popup.show()
    }

    private fun openLoginNetworkChoose() {
        CameraDialogs.openLoginNetworkChoose(fm = childFragmentManager) { net ->
            if (net == "wifi") {
                CameraDialogs.openLoginWifi(fm = childFragmentManager) { loggedIn ->
                    if (loggedIn) {
                        closeDrawer()
                    }
                }

            } else if (net == "lte") {
                CameraDialogs.openLoginLte(fm = childFragmentManager, cameraIp = null) { loggedIn ->
                    if (loggedIn) {
                        closeDrawer()
                    }
                }
            }
        }
    }

    /**
     * 백키 핸들러 - 오른쪽 메뉴가 열려있으면 닫히도록
     */
    private val sideMenuCloseBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            mBind.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * 백키 핸들러 - 백키를 두번 눌러야 종료되도록
     */
    private val twiceBackPressedCallback by lazy {
        TwiceBackPressedCallback(
            context = requireContext(),
            lifecycleScope = lifecycleScope,
            message = resStr(R.string.msg_one_more_back_button),
        )
    }

    /**
     * 네트워크 설정
     */
    private fun openNetworkMediaSetting() {
        val cfg = mViewModel.camManager.config ?: return
        CameraDialogs.openNetworkMedia(
            fm = childFragmentManager,
            media = cfg.enabledNetworkMedia,
        )
    }

    /**
     * 녹화 설정 다이얼로그
     */
    private fun openRecordingSchedule() {
        val schedule = mViewModel.camManager.config?.recordingSchedule ?: return
        CameraDialogs.openRecordingSchedule(
            fm = childFragmentManager,
            disabled = schedule.disabled,
            startTime = schedule.startTimestamp ?: Instant.now(),
            durationMinute = schedule.durationMinute
        ) {}
    }
}
