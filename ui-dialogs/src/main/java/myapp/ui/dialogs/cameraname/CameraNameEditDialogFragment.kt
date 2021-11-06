package myapp.ui.dialogs.cameraname

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import myapp.error.AppException
import myapp.extensions.trimOrEmpty
import myapp.ui.dialogs.databinding.DialogCameraNameEditBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.validator.CameraNameValidator
import splitties.fragmentargs.arg
import splitties.snackbar.snack

@AndroidEntryPoint
class CameraNameEditDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(cameraName: String) = CameraNameEditDialogFragment().apply {
            mArgCameraName = cameraName
        }
    }

    private val mViewModel: CameraNameEditDialogViewModel by viewModels()

    /**
     * Argument: 다이얼로그 dismiss 콜백
     */
    var onDismissListener: Action1<String?>? = null
    private var mArgCameraName: String by arg()
    private var mResultCameraName: String? = null

    private lateinit var mBind: DialogCameraNameEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = DialogCameraNameEditBinding.inflate(inflater, container, false)
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
        mBind.editText.multilineDone()
        mBind.editText.setText(mArgCameraName)
    }

    private fun setupEvents() {
        // 취소버튼 클릭
        mBind.txtviewCancelBtn.setOnClickListener {
            dismiss()
        }

        mBind.editText.setOnEditorActionListener { _, actionId, _ ->
            if (EditorInfo.IME_ACTION_DONE == actionId) {
                AndroidUtils.hideKeyboard(mBind.editText)
                true
            } else {
                false
            }
        }

        // 완료버튼 클릭
        mBind.txtviewSaveBtn.setOnClickListener {
            AndroidUtils.hideKeyboard(mBind.editText)
            lifecycleScope.launch {
                trySave(mBind.editText.trimOrEmpty())
            }
        }
    }

    private fun EditText.multilineDone() {
        imeOptions = EditorInfo.IME_ACTION_DONE
        inputType = EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE
        maxLines = Integer.MAX_VALUE
        setHorizontallyScrolling(false)
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

    private suspend fun trySave(cameraName: String) {
        val cameraIp = mViewModel.camManager.cameraIp
        if (cameraIp == null) {
            mBind.root.snack("카메라 연결을 확인해주세요")
            return
        }

        if (cameraName.isBlank()) {
            mBind.root.snack("카메라 이름을 입력해주세요")
            return
        }

        if (cameraName.length < 2 || cameraName.length > 30) {
            mBind.root.snack("카메라 이름을 2~30 글자로 입력해주세요")
            return
        }

        if (!CameraNameValidator.isValid(cameraName)) {
            mBind.root.snack("카메라 이름이 유효하지 않습니다")
            return
        }

        try {
            mViewModel.doSaveCameraName(ip = cameraIp, cameraName = cameraName)
            mBind.root.snack("저장되었습니다")
            mResultCameraName = cameraName
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
        onDismissListener?.invoke(mResultCameraName)
    }

}
