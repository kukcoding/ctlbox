package myapp.ui.dialogs.loginwifi

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import myapp.error.AppException
import myapp.extensions.trimOrEmpty
import myapp.ui.dialogs.databinding.DialogLoginWifiBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import splitties.snackbar.snack

@AndroidEntryPoint
class LoginWifiDialogFragment : DialogFragment() {
    companion object {
        fun newInstance() = LoginWifiDialogFragment()
    }

    private val mViewModel: LoginWifiDialogViewModel by viewModels()

    /**
     * Argument: 다이얼로그 dismiss 콜백
     */
    var onDismissListener: Action1<Boolean>? = null
    private var mResultLoggedIn = false

    private lateinit var mBind: DialogLoginWifiBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = DialogLoginWifiBinding.inflate(inflater, container, false)
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
        mBind.txtviewCancelBtn.setOnClickListener { dismiss() }

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
            AndroidUtils.hideKeyboard(mBind.edtxtPw)
            lifecycleScope.launch {
                tryLogin(pw = mBind.edtxtPw.trimOrEmpty())
            }
        }

        mViewModel.wifiStateFlow.asLiveData().observe(viewLifecycleOwner, { enabled ->
            mBind.imgviewWifi.isSelected = enabled
        })

        // 와이파이 아이콘 클릭시 와이파이 설정화면으로 이동
        mBind.layoutWifiBtn.setOnClickListener { openWifiSetting() }
    }

    /**
     * 와이파이 설정 화면으로 이동
     */
    private fun openWifiSetting() {
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    private fun preferWindowWidth(ctx: Context): Int {
        val preferWidth = AndroidUtils.dpf(300)
        val screenWidth = AndroidUtils.screenSmallSide(ctx)
        return minOf(preferWidth, screenWidth * 0.95f).toInt()
    }


    override fun onStart() {
        super.onStart()
        val window = dialog?.window ?: return
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(preferWindowWidth(requireContext()), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private suspend fun tryLogin(pw: String) {
        if (pw.isBlank()) {
            mBind.root.snack("비밀번호를 입력해주세요")
            return
        }

        lifecycleScope.launch {
            try {
                mViewModel.tryLogin(pw = pw)
                mResultLoggedIn = true
                mBind.root.snack("로그인되었습니다")
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
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultLoggedIn)
    }

}
