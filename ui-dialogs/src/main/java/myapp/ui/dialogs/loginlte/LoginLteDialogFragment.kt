package myapp.ui.dialogs.loginlte

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import myapp.error.AppException
import myapp.extensions.trimOrEmpty
import myapp.ui.dialogs.databinding.DialogLoginLteBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.validator.IPv4Validator
import splitties.fragmentargs.argOrNull
import splitties.snackbar.snack

@AndroidEntryPoint
class LoginLteDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(cameraIp: String?) = LoginLteDialogFragment().apply {
            mArgCameraIp = cameraIp
        }
    }

    private val mViewModel: LoginLteDialogViewModel by viewModels()

    private var mArgCameraIp by argOrNull<String>()

    /**
     * Argument: 다이얼로그 dismiss 콜백
     */
    var onDismissListener: Action1<Boolean>? = null
    private var mResultLoggedIn = false

    private lateinit var mBind: DialogLoginLteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = DialogLoginLteBinding.inflate(inflater, container, false)
        mBind.vm = mViewModel
        mBind.lifecycleOwner = this
        return mBind.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customInit()
        setupEvents()
        if (!mArgCameraIp.isNullOrBlank()) {
            mBind.edtxtIp.setText(mArgCameraIp)
        }
    }

    private fun customInit() {
    }

    private fun setupEvents() {
        // 취소버튼 클릭
        mBind.txtviewCancelBtn.setOnClickListener {
            dismiss()
        }

        mBind.edtxtPw.setOnEditorActionListener { _, actionId, _ ->
            if (EditorInfo.IME_ACTION_DONE == actionId) {
                AndroidUtils.hideKeyboard(mBind.edtxtPw)
                true
            } else {
                false
            }
        }

        // 로그인 버튼 클릭
        mBind.txtviewSaveBtn.setOnClickListener {
            AndroidUtils.hideKeyboard(mBind.edtxtIp, mBind.edtxtPw)
            lifecycleScope.launch {
                tryLogin(ip = mBind.edtxtIp.trimOrEmpty(), pw = mBind.edtxtPw.trimOrEmpty())
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


    private suspend fun tryLogin(ip: String, pw: String) {

        if (ip.isBlank()) {
            mBind.root.snack("카메라의 IP 주소를 입력해주세요")
            return
        }

        if (!IPv4Validator.isValid(ip)) {
            mBind.root.snack("IP 주소 포맷이 올바르지 않습니다")
            return
        }

        if (pw.isBlank()) {
            mBind.root.snack("비밀번호를 입력해주세요")
            return
        }

        try {
            mViewModel.tryLogin(ip = ip, pw = pw)
            mBind.root.snack("로그인되었습니다")
            mResultLoggedIn = true
            delay(400)
            dismiss()
        } catch (e: Throwable) {
            if (e is AppException) {
                mBind.root.snack(e.displayMessage())
            } else {
                mBind.root.snack("로그인 실패: ${e.message}")
            }
            e.printStackTrace()
        }
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultLoggedIn)
    }

}
