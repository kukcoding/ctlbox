package myapp.ui.dialogs.streamquality


import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import myapp.data.code.VideoQualityKind
import myapp.error.AppException
import myapp.ui.dialogs.databinding.DialogStreamQualityBinding
import myapp.ui.widget.ResolutionRadioButtons
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.util.AndroidUtils.dpf
import splitties.fragmentargs.arg
import splitties.snackbar.snack
import kotlin.math.min


@AndroidEntryPoint
class StreamQualityDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(
            videoQualityKind: VideoQualityKind,
            title: String,
            fps: Int,
            resolution: String,
            availableResolutions: List<String>
        ) = StreamQualityDialogFragment().apply {
            mArgVideoQualityKind = videoQualityKind
            mArgTitle = title
            mArgFps = fps
            mArgResolution = resolution
            mArgAvailableResolutions = ArrayList(availableResolutions)
        }
    }

    // argument
    private var mArgTitle: String by arg()
    private var mArgVideoQualityKind: VideoQualityKind by arg()
    private var mArgResolution: String by arg()
    private var mArgAvailableResolutions: ArrayList<String> by arg()
    private var mArgFps: Int by arg()

    private var mResultChanged = false
    var onDismissListener: Action1<Boolean>? = null

    private val mViewModel: StreamQualityDialogViewModel by viewModels()
    private lateinit var mBind: DialogStreamQualityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onDismissListener == null) {
            dismiss()
        }
        if (savedInstanceState == null) {
            mViewModel.submitAction(
                StreamQualityAction.Setup(
                    videoQualityKind = mArgVideoQualityKind,
                    fps = mArgFps,
                    resolution = mArgResolution,
                    availableResolutions = mArgAvailableResolutions
                )
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBind = DialogStreamQualityBinding.inflate(inflater, container, false)
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
        mBind.txtviewDialogTitle.text = mArgTitle
    }

    private fun setupEvents() {

        mViewModel.liveFieldOf(StreamQualityViewState::availableResolutions).observe(viewLifecycleOwner, {
            mBind.resolutionRadioButtons.resolutions = it
        })

        mViewModel.liveFieldOf(StreamQualityViewState::resolution).observe(viewLifecycleOwner, {
            mBind.resolutionRadioButtons.selectedResolution = it
        })

        // ????????? ?????? ?????????
        mBind.resolutionRadioButtons.onSelectListener = object : ResolutionRadioButtons.OnSelectListener {
            override fun onSelect(resolution: String) {
                mViewModel.submitAction(StreamQualityAction.SetResolution(resolution))
            }
        }


        // ???????????? ??????
        mBind.btDone.setOnClickListener {
            lifecycleScope.launch {
                trySave()
            }
        }

        // ?????? ?????? ??????
        mBind.layoutCloseBtn.setOnClickListener {
            dismiss()
        }
    }


    private suspend fun trySave() {
        val cameraIp = mViewModel.camManager.cameraIp
        if (cameraIp == null) {
            mBind.root.snack("????????? ????????? ??????????????????")
            return
        }

        val state = mViewModel.currentState()
        val resolution = state.resolution
        val fps = state.fps

        try {
            mViewModel.saveVideoQuality(ip = cameraIp, resolution = resolution, fps = fps)
            mResultChanged = true
            mBind.root.snack("?????????????????????")
            delay(400)
            dismiss()
        } catch (e: Throwable) {
            if (e is AppException) {
                mBind.root.snack(e.displayMessage())
            } else {
                mBind.root.snack("?????? ??????: ${e.message}")
            }
            e.printStackTrace()
        }

    }


    override fun onStart() {
        super.onStart()
        val ctx = this.context ?: return
        val window = dialog?.window ?: return
        val w = preferWindowWidth(ctx)
        val h = AndroidUtils.screenHeight(ctx) * 0.7
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(w.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun preferWindowWidth(ctx: Context): Float {
        val preferWidth = dpf(320)
        val screenWidth = AndroidUtils.screenSmallSide(ctx)
        return if (preferWidth < 0) preferWidth else min(preferWidth, screenWidth * 0.85f)
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultChanged)
    }
}

