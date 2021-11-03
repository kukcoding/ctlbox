package myapp.ui.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import myapp.ui.dialogs.CameraDialogs
import myapp.ui.dialogs.streamquality.StreamQuality
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = FragmentSettingsBinding.inflate(inflater, container, false)
        mBind.lifecycleOwner = this
        mBind.vm = mViewModel
        mBind.fragment = this
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

    }

    fun openNetworkMediaSetting() {
        CameraDialogs.openNetworkMedia(
            fm = childFragmentManager,
            media = "wifi,lte",
        ) { result ->
            //
        }
    }

    fun openRecordQuality() {
        CameraDialogs.openStreamQuality(
            fm = childFragmentManager,
            title = "녹화 품질",
            streamQuality = StreamQuality(fps = 25, resolution = 15),
        ) { result ->
            //
        }
    }

    fun openStreamQuality() {
        CameraDialogs.openStreamQuality(
            fm = childFragmentManager,
            title = "스트리밍 품질",
            streamQuality = StreamQuality(fps = 25, resolution = 15),
        ) { result ->
            //
        }
    }

    fun openCameraNameEdit() {
        CameraDialogs.openCameraNameEdit(
            fm = childFragmentManager,
            cameraName = "camera name",
        ) { result ->
            //
        }
    }

    fun openCameraWifiEdit() {
        CameraDialogs.openCameraWifiEdit(
            fm = childFragmentManager,
            ssid = "IPCAM",
        ) { result ->
            //
        }
    }

    fun openCameraPwEdit() {
        CameraDialogs.openCameraPasswordEdit(
            fm = childFragmentManager,
        ) { result ->
            //
        }
    }
}
