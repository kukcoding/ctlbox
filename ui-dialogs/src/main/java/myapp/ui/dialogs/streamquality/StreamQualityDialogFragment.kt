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
import dagger.hilt.android.AndroidEntryPoint
import myapp.ui.dialogs.databinding.DialogStreamQualityBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.util.AndroidUtils.dpf
import kotlin.math.min

data class StreamQuality(
    val fps: Int,
    val resolution: String
)

@AndroidEntryPoint
class StreamQualityDialogFragment : DialogFragment() {
    companion object {
        fun newInstance() = StreamQualityDialogFragment()
    }

    // argument
    lateinit var mArgTitle: String
    lateinit var mArgStreamQuality: StreamQuality
    private var mResultStreamQuality: StreamQuality? = null

    var onDismissListener: Action1<StreamQuality?>? = null

    private val mViewModel: StreamQualityDialogViewModel by viewModels()
    private lateinit var mBind: DialogStreamQualityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onDismissListener == null) {
            dismiss()
        }
        if (savedInstanceState == null) {
            mViewModel.updateFps(mArgStreamQuality.fps)
            mViewModel.updateResolution(mArgStreamQuality.resolution)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBind = DialogStreamQualityBinding.inflate(inflater, container, false)
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
        mBind.txtviewDialogTitle.text = mArgTitle
    }

    private fun setupEvents() {
        // 완료버튼 클릭
        mBind.btDone.setOnClickListener {
            val state = mViewModel.currentState()
            dismissWithDataChanged(StreamQuality(fps = state.fps, resolution = state.resolution))
        }

        // 닫기 버튼 클릭
        mBind.layoutCloseBtn.setOnClickListener {
            dismissWithDataChanged(null)
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

    private fun dismissWithDataChanged(value: StreamQuality?) {
        mResultStreamQuality = value
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultStreamQuality)
    }
}

