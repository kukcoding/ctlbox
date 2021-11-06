package myapp.ui.dialogs.networkmedia


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
import myapp.error.AppException
import myapp.ui.dialogs.databinding.DialogNetworkMediaBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.util.AndroidUtils.dpf
import splitties.fragmentargs.arg
import splitties.snackbar.snack
import kotlin.math.min


@AndroidEntryPoint
class NetworkMediaDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(media: String) = NetworkMediaDialogFragment().apply {
            mArgNetworkMedia = media
        }
    }

    // argument
    private var mArgNetworkMedia: String by arg()
    private var mResultNetworkMedia: String? = null

    var onDismissListener: Action1<String?>? = null

    private val mViewModel: NetworkMediaDialogViewModel by viewModels()
    private lateinit var mBind: DialogNetworkMediaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onDismissListener == null) {
            dismiss()
        }
        if (savedInstanceState == null) {
            mViewModel.updateMedia(mArgNetworkMedia)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBind = DialogNetworkMediaBinding.inflate(inflater, container, false)
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
    }

    private fun setupEvents() {
        // 완료버튼 클릭
        mBind.btDone.setOnClickListener {
            val state = mViewModel.currentState()
            lifecycleScope.launch {
                trySave(media = state.media)
            }
        }

        // 닫기 버튼 클릭
        mBind.layoutCloseBtn.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val ctx = this.context ?: return
        val window = dialog?.window ?: return
        val w = preferWindowWidth(ctx)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(w.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun preferWindowWidth(ctx: Context): Float {
        val preferWidth = dpf(260)
        val screenWidth = AndroidUtils.screenSmallSide(ctx)


        return if (preferWidth < 0) preferWidth else min(preferWidth, screenWidth * 0.85f)
    }

    private suspend fun trySave(media: String) {
        val cameraIp = mViewModel.camManager.cameraIp
        if (cameraIp == null) {
            mBind.root.snack("카메라 연결을 확인해주세요")
            return
        }

        try {
            mViewModel.saveNetworkMedia(ip = cameraIp, media = media)
            mResultNetworkMedia = media
            mBind.root.snack("저장되었습니다")
            delay(400)
            dismiss()
        } catch (e: Throwable) {
            if (e is AppException) {
                mBind.root.snack(e.displayMessage())
            } else {
                mBind.root.snack("에러 발생: ${e.message}")
            }
            e.printStackTrace()
        }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultNetworkMedia)
    }
}

