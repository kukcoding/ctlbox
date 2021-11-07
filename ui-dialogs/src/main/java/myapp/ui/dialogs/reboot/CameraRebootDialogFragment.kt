package myapp.ui.dialogs.reboot

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import myapp.BuildVars
import myapp.error.AppException
import myapp.ui.dialogs.databinding.DialogCameraRebootBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import splitties.snackbar.snack

@AndroidEntryPoint
class CameraRebootDialogFragment : DialogFragment() {
    companion object {
        fun newInstance() = CameraRebootDialogFragment()
    }

    private val mViewModel: CameraRebootDialogViewModel by viewModels()

    /**
     * Argument: 다이얼로그 dismiss 콜백
     */
    var onDismissListener: Action1<Boolean>? = null
    private var mResultRebooted = false

    private lateinit var mBind: DialogCameraRebootBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = DialogCameraRebootBinding.inflate(inflater, container, false)
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
        mBind.txtviewMsg3.text = "재부팅 시간은 보통 ${BuildVars.rebootDurationSec}초 정도이지만,\n장치 상태에 따라 조금 더 걸릴 수 있습니다"
    }

    private fun setupEvents() {
        // 닫기 버튼 클릭
        mBind.btClose.setOnClickListener {
            dismiss()
        }


        // 재부팅 시작 버튼 클릭
        mBind.btStartReboot.setOnClickListener {
            lifecycleScope.launch {
                tryReboot()
            }
        }


        // 상태에 따라 UI 조정
        mViewModel.liveFieldOf(RebootDialogState::step).observe(viewLifecycleOwner, { step ->
            when (step) {
                is RebootStep.None -> {
                    isCancelable = true
                    mBind.layoutRebooting.isVisible = false
                    mBind.layoutCompleted.isVisible = false
                    mBind.btStartReboot.isEnabled = true
                }
                is RebootStep.RebootingStarted -> {
                    isCancelable = false
                    mBind.layoutRebooting.isVisible = true
                    mBind.layoutCompleted.isVisible = false
                    mBind.btStartReboot.isEnabled = false
                    mBind.btStartReboot.alpha = 0.3f
                }

                is RebootStep.FinishWait -> {
                    isCancelable = true
                    mResultRebooted = true
                    mBind.layoutRebooting.isVisible = false
                    mBind.layoutCompleted.isVisible = true
                    mBind.btStartReboot.isEnabled = false
                    mBind.btStartReboot.alpha = 0.3f
                }
            }
        })

        // 재부팅 시간 갱신
        mViewModel.rebootTimeTextLive.observe(viewLifecycleOwner, {
            mBind.txtviewRebootTime.text = it
        })

        // 재부팅 진행상태바 갱신
        mViewModel.rebootProgressLive.observe(viewLifecycleOwner, {
            mBind.progressBarReboot.setProgress(it, false)
        })


        // 재부팅 완료 버튼 클릭
        mBind.btRebootingDone.setOnClickListener {
            mResultRebooted = true
            dismiss()
        }
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

    private suspend fun tryReboot() {
        val cameraIp = mViewModel.camManager.cameraIp
        if (cameraIp == null) {
            mBind.root.snack("카메라 연결을 확인해주세요")
            return
        }

        try {
            mViewModel.reboot(ip = cameraIp)
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
        onDismissListener?.invoke(mResultRebooted)
    }

}
