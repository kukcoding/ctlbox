package myapp.ui.dialogs.recordingschedule


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
import com.afollestad.date.dayOfMonth
import com.afollestad.date.month
import com.afollestad.date.year
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.datePicker
import com.afollestad.materialdialogs.datetime.timePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import myapp.data.code.RecordingScheduleType
import myapp.error.AppException
import myapp.extensions.alert
import myapp.ui.dialogs.databinding.DialogRecordingScheduleBinding
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.util.AndroidUtils.dpf
import org.threeten.bp.Instant
import org.threeten.bp.ZonedDateTime
import splitties.fragmentargs.arg
import splitties.snackbar.snack
import java.util.*
import kotlin.math.min


@AndroidEntryPoint
class RecordingScheduleDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(
            disabled: Boolean,
            startTime: Instant,
            durationMinute: Long
        ) = RecordingScheduleDialogFragment().apply {
            mArgDisabled = disabled
            mArgStartTime = startTime
            mArgDurationMinute = durationMinute
        }
    }

    // argument
    private var mArgDisabled: Boolean by arg()
    private var mArgStartTime: Instant by arg()
    private var mArgDurationMinute: Long by arg()

    private var mResultChanged = false
    var onDismissListener: Action1<Boolean>? = null

    private val mViewModel: RecordingScheduleViewModel by viewModels()
    private lateinit var mBind: DialogRecordingScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onDismissListener == null) {
            dismiss()
        }

        if (savedInstanceState == null) {
            mViewModel.submitAction(
                RecordingScheduleAction.Setup(
                    disabled = mArgDisabled,
                    startTime = mArgStartTime,
                    durationMinute = mArgDurationMinute,
                )
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBind = DialogRecordingScheduleBinding.inflate(inflater, container, false)
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
    }

    private fun setupEvents() {

        // 완료버튼 클릭
        mBind.btDone.setOnClickListener {
            lifecycleScope.launch {
                trySave()
            }
        }

        // 닫기 버튼 클릭
        mBind.layoutCloseBtn.setOnClickListener {
            dismiss()
        }

        mBind.txtviewRecordingStartTime1.setOnClickListener { openStartDatePicker() }
        mBind.txtviewRecordingStartTime2.setOnClickListener { openStartTimePicker() }
        mBind.txtviewRecordingFinishTime1.setOnClickListener { openFinishDatePicker() }
        mBind.txtviewRecordingFinishTime2.setOnClickListener { openFinishTimePicker() }
    }


    private suspend fun trySave() {
        val cameraIp = mViewModel.camManager.cameraIp
        if (cameraIp == null) {
            mBind.root.snack("카메라 연결을 확인해주세요")
            return
        }

        val state = mViewModel.currentState()
        val disabled = state.scheduleType == RecordingScheduleType.DISABLED
        val durationMinute = if (state.scheduleType == RecordingScheduleType.FINITE) state.durationMinute else -1L
        val startTime = state.startTime.toInstant()

        if (state.scheduleType == RecordingScheduleType.FINITE && durationMinute <= 0) {
            alert("녹화 종료 시간을 시작 시간보다 크게 입력해주세요")
            return
        }

        try {
            mViewModel.doSaveSchedule(
                ip = cameraIp,
                disabled = disabled,
                startTime = startTime,
                durationMinute = durationMinute
            )
            mResultChanged = true
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


    override fun onStart() {
        super.onStart()
        val ctx = this.context ?: return
        val window = dialog?.window ?: return
        val w = preferWindowWidth(ctx)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(w.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun preferWindowWidth(ctx: Context): Float {
        val preferWidth = dpf(340)
        val screenWidth = AndroidUtils.screenSmallSide(ctx)
        return if (preferWidth < 0) preferWidth else min(preferWidth, screenWidth * 0.85f)
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultChanged)
    }

    private fun openStartDatePicker() {
        val current = mViewModel.currentState().startTime
        val cal = Calendar.getInstance()
        cal.set(current.year, current.month.value, current.dayOfMonth)
        MaterialDialog(requireContext()).show {
            datePicker(currentDate = cal) { _, datetime ->
                mViewModel.submitAction(
                    RecordingScheduleAction.SetStartDate(
                        datetime.year,
                        datetime.month + 1,
                        datetime.dayOfMonth
                    )
                )
            }
        }
    }

    private fun openStartTimePicker() {
        val current = mViewModel.currentState().startTime
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, current.hour)
        cal.set(Calendar.MINUTE, current.minute)
        MaterialDialog(requireContext()).show {
            timePicker(show24HoursView = true, currentTime = cal) { _, datetime ->
                mViewModel.submitAction(
                    RecordingScheduleAction.SetStartTime(
                        datetime.get(Calendar.HOUR_OF_DAY),
                        datetime.get(Calendar.MINUTE),
                    )
                )
            }
        }
    }

    private fun openFinishDatePicker() {
        val current = mViewModel.currentState().finishTime ?: ZonedDateTime.now()
        val cal = Calendar.getInstance()
        cal.set(current.year, current.month.value, current.dayOfMonth)
        MaterialDialog(requireContext()).show {
            datePicker(currentDate = cal) { _, datetime ->
                mViewModel.submitAction(
                    RecordingScheduleAction.SetFinishDate(
                        datetime.year,
                        datetime.month + 1,
                        datetime.dayOfMonth
                    )
                )
            }
        }
    }

    private fun openFinishTimePicker() {
        val current = mViewModel.currentState().finishTime ?: ZonedDateTime.now()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, current.hour)
        cal.set(Calendar.MINUTE, current.minute)
        MaterialDialog(requireContext()).show {
            timePicker(show24HoursView = true, currentTime = cal) { _, datetime ->
                mViewModel.submitAction(
                    RecordingScheduleAction.SetFinishTime(
                        datetime.get(Calendar.HOUR_OF_DAY),
                        datetime.get(Calendar.MINUTE),
                    )
                )
            }
        }
    }
}

