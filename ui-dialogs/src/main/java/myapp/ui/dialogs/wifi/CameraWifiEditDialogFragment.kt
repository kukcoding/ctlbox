package myapp.ui.dialogs.wifi

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import myapp.extensions.trimOrEmpty
import myapp.ui.dialogs.databinding.DialogCameraWifiEditBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import splitties.fragmentargs.arg

@AndroidEntryPoint
class CameraWifiEditDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(ssid: String) = CameraWifiEditDialogFragment().apply {
            mArgSsid = ssid
        }
    }

    private val mViewModel: CameraWifiEditDialogViewModel by viewModels()

    /**
     * Argument: 다이얼로그 dismiss 콜백
     */
    var onDismissListener: Action1<String?>? = null
    private var mArgSsid: String by arg()
    private var mResultCameraName: String? = null

    private lateinit var mBind: DialogCameraWifiEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = DialogCameraWifiEditBinding.inflate(inflater, container, false)
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
        mBind.editTextSsid.setText(mArgSsid)
    }

    private fun setupEvents() {
        // 취소버튼 클릭
        mBind.txtviewCancelBtn.setOnClickListener {
            mResultCameraName = null
            dismiss()
        }

        mBind.editTextSsid.setOnEditorActionListener { _, actionId, _ ->
            if (EditorInfo.IME_ACTION_DONE == actionId) {
                AndroidUtils.hideKeyboard(mBind.editTextSsid)
                true
            } else {
                false
            }
        }

        // 완료버튼 클릭
        mBind.txtviewSaveBtn.setOnClickListener {
            doSave(mBind.editTextSsid.trimOrEmpty())
        }
    }


    private fun preferWindowWidth(ctx: Context): Int {
        val preferWidth = AndroidUtils.dpf(360)
        val screenWidth = AndroidUtils.screenSmallSide(ctx)
        return minOf(preferWidth, screenWidth * 0.95f).toInt()
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window ?: return
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(preferWindowWidth(requireContext()), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun doSave(cameraName: String?) {
        lifecycleScope.launch {
//            try {
//                val card = mViewModel.saveBibleCardMessage(cameraName = cameraName)
//                dismissWithSavedCard(cameraName = card)
//            } catch (e: Throwable) {
//                mBind.root.snack(trimNull(e.message) ?: "에러가 발생했습니다")
//            }
        }
    }

    private fun dismissWithSavedCard(cameraName: String?) {
        mResultCameraName = cameraName
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultCameraName)
    }

}
