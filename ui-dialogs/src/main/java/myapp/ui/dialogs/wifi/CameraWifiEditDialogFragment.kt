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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import myapp.error.AppException
import myapp.extensions.trimOrEmpty
import myapp.ui.dialogs.databinding.DialogCameraWifiEditBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import splitties.fragmentargs.argOrNull
import splitties.snackbar.snack

@AndroidEntryPoint
class CameraWifiEditDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(wifiSsid: String?, wifiPw: String?) = CameraWifiEditDialogFragment().apply {
            mArgWifiSsid = wifiSsid
            mArgWifiPw = wifiPw
        }
    }

    private val mViewModel: CameraWifiEditDialogViewModel by viewModels()

    /**
     * Argument: 다이얼로그 dismiss 콜백
     */
    var onDismissListener: Action1<String?>? = null
    private var mArgWifiSsid: String? by argOrNull()
    private var mArgWifiPw: String? by argOrNull()
    private var mResultSsid: String? = null

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
        if (savedInstanceState == null) {
            mViewModel.updateArgs(wifiSsid = mArgWifiSsid, wifiPw = mArgWifiPw)
        }
        customInit()
        setupEvents()
    }

    private fun customInit() {
    }

    private fun setupEvents() {
        // 취소버튼 클릭
        mBind.txtviewCancelBtn.setOnClickListener {
            mResultSsid = null
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
            trySave(mBind.editTextSsid.trimOrEmpty(), mBind.editTextSsid.trimOrEmpty())
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

    private fun trySave(wifiSsid: String, wifiPw: String) {
        if (wifiSsid.isBlank()) {
            mBind.root.snack("SSID를 입력해주세요")
            return
        }
        if (wifiPw.isBlank()) {
            mBind.root.snack("비밀번호를 입력해주세요")
            return
        }

        val clientIp = mViewModel.camManager.cameraIp
        if (clientIp == null) {
            mBind.root.snack("카메라 연결을 확인해주세요")
            return
        }

        lifecycleScope.launch {
            try {
                mViewModel.saveWifi(ip = clientIp, wifiSsid = wifiSsid, wifiPw = wifiPw)
                mBind.root.snack("저장되었습니다")
                delay(400)
                dismissWithSaved(wifiSsid)
            } catch (e: Throwable) {
                if (e is AppException) {
                    mBind.root.snack(e.displayMessage())
                } else {
                    mBind.root.snack("에러 발생: ${e.message}")
                }
                e.printStackTrace()
            }
        }
    }

    private fun dismissWithSaved(wifiSsid: String) {
        mResultSsid = wifiSsid
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultSsid)
    }

}
