package myapp.ui.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import myapp.data.code.VideoQualityKind
import myapp.ui.dialogs.CameraDialogs
import myapp.ui.settings.databinding.FragmentSettingsBinding


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment().apply {
        }
    }

    private val mViewModel: SettingsViewModel by viewModels()
    private lateinit var mBind: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = FragmentSettingsBinding.inflate(inflater, container, false)
        mBind.lifecycleOwner = this
        mBind.vm = mViewModel
        return mBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customInit()
        setupEvents()
    }

    private fun customInit() {
    }

    private fun setupEvents() {
        mBind.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        // 네트워크 미디어 설정 팝업
        mBind.layoutNetworkMediaSettingBtn.setOnClickListener {
            openNetworkMediaSetting()
        }

        // 카메라 비번 변경 팝업
        mBind.layoutCameraPwEditBtn.setOnClickListener {
            openCameraPwEdit()
        }

        // 스트리밍 품질 팝업
        mBind.layoutStreamQualityBtn.setOnClickListener {
            openStreamQuality()
        }

        // 녹화 품질 설정 팝업
        mBind.layoutRecordQualityBtn.setOnClickListener {
            openRecordQuality()
        }

        // 와이파이 설정 팝업
        mBind.layoutWifiSettingBtn.setOnClickListener {
            openCameraWifiEdit()
        }

        // 카메라 이름 수정 팝업
        mBind.layoutCameraNameEditBtn.setOnClickListener {
            openCameraNameEdit()
        }

        // 재부팅 팝업
        mBind.layoutRebootBtn.setOnClickListener {
            confirmReboot()
        }

    }

    private fun openNetworkMediaSetting() {
        val cfg = mViewModel.camManager.config ?: return
        CameraDialogs.openNetworkMedia(
            fm = childFragmentManager,
            media = cfg.enabledNetworkMedia,
        )
    }

    private fun openRecordQuality() {
        val cfg = mViewModel.camManager.config ?: return

        CameraDialogs.openStreamQuality(
            fm = childFragmentManager,
            videoQualityKind = VideoQualityKind.RECORDING,
            title = "녹화 품질",
            fps = cfg.recording.fps,
            resolution = cfg.recording.resolution,
            availableResolutions = cfg.recordingResolutions
        )
    }

    private fun openStreamQuality() {
        val cfg = mViewModel.camManager.config ?: return
        CameraDialogs.openStreamQuality(
            fm = childFragmentManager,
            videoQualityKind = VideoQualityKind.STREAMING,
            title = "스트리밍 품질",
            fps = cfg.streaming.fps,
            resolution = cfg.streaming.resolution,
            availableResolutions = cfg.streamingResolutions
        )
    }

    private fun openCameraNameEdit() {
        val cfg = mViewModel.camManager.config ?: return
        CameraDialogs.openCameraNameEdit(
            fm = childFragmentManager,
            cameraName = cfg.cameraName ?: "",
        )
    }

    private fun openCameraWifiEdit() {
        val cfg = mViewModel.camManager.config ?: return
        CameraDialogs.openCameraWifiEdit(
            fm = childFragmentManager,
            wifiSsid = cfg.wifiSsid,
            wifiPw = cfg.wifiPw,
        )
    }

    private fun openCameraPwEdit() {
        CameraDialogs.openCameraPasswordEdit(fm = childFragmentManager)
    }

    private fun confirmReboot() {
        val lastCameraIp = mViewModel.camManager.cameraIp
        CameraDialogs.openReboot(fm = childFragmentManager) { rebooted ->
            if (rebooted) {
                // 재부팅후에 로그인 팝업을 띄운다
                lifecycleScope.launch {
                    delay(500)
                    if (isActive) {
                        openLogin(lastCameraIp = lastCameraIp)
                    }
                }
            }
        }
    }

    private fun openLogin(lastCameraIp: String?) {
        CameraDialogs.openLoginNetworkChoose(fm = childFragmentManager) { net ->
            if (net == "wifi") {
                CameraDialogs.openLoginWifi(fm = childFragmentManager)
            } else if (net == "lte") {
                CameraDialogs.openLoginLte(fm = childFragmentManager, cameraIp = lastCameraIp)
            }
        }
    }
}
