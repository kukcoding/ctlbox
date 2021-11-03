package myapp.ui.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
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

//        mBind.layoutEditBtn.setOnClickListener {
//            Toast.makeText(requireContext(), "편집", Toast.LENGTH_SHORT).show()
//        }

        mBind.layoutNetworkMediaSettingBtn.setOnClickListener {
            openNetworkMediaSetting()
        }
        mBind.layoutCameraPwEditBtn.setOnClickListener {
            openCameraPwEdit()
        }

        mBind.layoutStreamQualityBtn.setOnClickListener {
            openStreamQuality()
        }

        mBind.layoutRecordQualityBtn.setOnClickListener {
            openRecordQuality()
        }

        mBind.layoutWifiSettingBtn.setOnClickListener {
            openCameraWifiEdit()
        }

        mBind.layoutCameraNameEditBtn.setOnClickListener {
            openCameraNameEdit()
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
        CameraDialogs.openCameraWifiEdit(
            fm = childFragmentManager,
            ssid = "IPCAM",
        )
    }

    private fun openCameraPwEdit() {
        CameraDialogs.openCameraPasswordEdit(fm = childFragmentManager)
    }
}
