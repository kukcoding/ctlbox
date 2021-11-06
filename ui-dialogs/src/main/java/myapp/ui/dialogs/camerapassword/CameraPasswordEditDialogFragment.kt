package myapp.ui.dialogs.camerapassword

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
import myapp.extensions.trimOrEmpty
import myapp.ui.dialogs.databinding.DialogCameraPasswordEditBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.validator.CameraPasswordValidator
import splitties.snackbar.snack

@AndroidEntryPoint
class CameraPasswordEditDialogFragment : DialogFragment() {
    companion object {
        fun newInstance() = CameraPasswordEditDialogFragment().apply {
        }
    }

    private val mViewModel: CameraPasswordEditDialogViewModel by viewModels()

    /**
     * Argument: 다이얼로그 dismiss 콜백
     */
    var onDismissListener: Action1<Boolean>? = null

    private var mResultPwChanged = false

    private lateinit var mBind: DialogCameraPasswordEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = DialogCameraPasswordEditBinding.inflate(inflater, container, false)
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
        // 취소버튼 클릭
        mBind.txtviewCancelBtn.setOnClickListener {
            dismiss()
        }

        // 완료버튼 클릭
        mBind.txtviewSaveBtn.setOnClickListener {
            AndroidUtils.hideKeyboard(mBind.editTextPw, mBind.editTextPw2)
            lifecycleScope.launch {
                trySave(pw1 = mBind.editTextPw.trimOrEmpty(), pw2 = mBind.editTextPw2.trimOrEmpty())
            }
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

    private suspend fun trySave(pw1: String, pw2: String) {
        val cameraIp = mViewModel.camManager.cameraIp
        if (cameraIp == null) {
            mBind.root.snack("카메라 연결을 확인해주세요")
            return
        }

        if (pw1.isBlank() || pw2.isBlank()) {
            mBind.root.snack("비밀번호를 입력해주세요")
            return
        }
        if (pw1.length < 4 || pw1.length > 30) {
            mBind.root.snack("비밀번호를 4~30 글자로 입력해주세요")
            return
        }

        if (!CameraPasswordValidator.isValid(pw1)) {
            mBind.root.snack("비밀번호가 유효하지 않습니다")
            return
        }

        if (pw1 != pw2) {
            mBind.root.snack("입력하신 두 비밀번호가 일치하지 않습니다")
            return
        }

        try {
            mViewModel.doSaveCameraPw(ip = cameraIp, pw = pw1)
            mBind.root.snack("저장되었습니다")
            mResultPwChanged = true
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
        onDismissListener?.invoke(mResultPwChanged)
    }

}
