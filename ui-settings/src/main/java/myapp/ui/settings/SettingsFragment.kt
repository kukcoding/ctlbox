package myapp.ui.settings


import android.os.Bundle
import android.text.SpannedString
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import myapp.data.cam.RecordingState
import myapp.data.code.VideoQualityKind
import myapp.extensions.resources.resColor
import myapp.extensions.twoDigit
import myapp.ui.dialogs.CameraDialogs
import myapp.ui.settings.databinding.FragmentSettingsBinding
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

private fun formatDate(startTime: Instant): String {
    val from = startTime.atZone(ZoneId.systemDefault())
    return "${from.year}년 ${from.monthValue}월 ${from.dayOfMonth}일 ${from.hour}시 ${from.minute}분 ${from.second}초"
}

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = SettingsFragment().apply {
        }
    }

    private val mViewModel: SettingsViewModel by viewModels()
    private lateinit var mBind: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = FragmentSettingsBinding.inflate(inflater, container, false)
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
        mBind.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        // 네트워크 미디어 설정 팝업
        mBind.layoutNetworkMediaSettingBtn.setOnClickListener {
            openNetworkMediaSetting()
        }

        // 카메라 비번 변경 팝업
        mBind.layoutCameraPwEditBtn.setOnClickListener {
            openCameraPwEdit()
        }

        // 스트리밍 품질 팝업
        mBind.layoutStreamQualityBtn.setOnClickListener {
            openStreamQuality()
        }

        // 녹화 품질 설정 팝업
        mBind.layoutRecordQualityBtn.setOnClickListener {
            openRecordQuality()
        }

        // 와이파이 설정 팝업
        mBind.layoutWifiSettingBtn.setOnClickListener {
            openCameraWifiEdit()
        }

        // 카메라 이름 수정 팝업
        mBind.layoutCameraNameEditBtn.setOnClickListener {
            openCameraNameEdit()
        }

        // 재부팅 팝업
        mBind.layoutRebootBtn.setOnClickListener {
            openReboot()
        }

        // 녹화 상태 버튼 클릭
        mBind.layoutRecordingStateBtn.setOnClickListener {
            openRecordingSchedule()
        }

        // 녹화 스케줄 버튼 클릭
        mBind.layoutRecordingScheduleBtn.setOnClickListener {
            openRecordingSchedule()
        }

        mViewModel.recordingTracker.stateFlow.asLiveData().observe(viewLifecycleOwner, Observer { state ->

            when (state) {
                is RecordingState.Disabled -> {
                    val startTime = state.schedule?.startTimestamp
                    mBind.txtviewRecordScheduleType.text = "(비활성)"
                    if (startTime == null) {
                        mBind.txtviewRecordTime1.text = ""
                        mBind.txtviewRecordTime2.text = ""
                    } else {
                        mBind.txtviewRecordTime1.text = formatDate(startTime)
                        mBind.txtviewRecordTime2.text = ""
                    }
                }
                is RecordingState.FiniteRecording -> {
                    mBind.txtviewRecordScheduleType.text = "(시간 지정 녹화)"
                    val startTime = state.schedule.startTimestamp
                    val durationMinute = state.schedule.durationMinute
                    if (startTime != null) {
                        mBind.txtviewRecordTime1.text = formatRecordTime1(startTime, durationMinute)
                        mBind.txtviewRecordTime2.text = formatRecordTime2(startTime, durationMinute)
                    }
                }
                is RecordingState.InfiniteRecording -> {
                    mBind.txtviewRecordScheduleType.text = "(상시 녹화)"
                    val startTime = state.schedule.startTimestamp
                    val durationMinute = state.schedule.durationMinute
                    if (startTime != null) {
                        mBind.txtviewRecordTime1.text = formatRecordTime1(startTime, durationMinute)
                        mBind.txtviewRecordTime2.text = formatRecordTime2(startTime, durationMinute)
                    }
                }
                is RecordingState.RecordingScheduled -> {
                    val durationMinute = state.schedule.durationMinute
                    mBind.txtviewRecordScheduleType.text = if(durationMinute <= 0) "(상시 녹화 예약)" else "(녹화 예약)"
                    val startTime = state.schedule.startTimestamp
                    if (startTime != null) {
                        mBind.txtviewRecordTime1.text = formatRecordTime1(startTime, durationMinute)
                        mBind.txtviewRecordTime2.text = formatRecordTime2(startTime, durationMinute)
                    }
                }
                is RecordingState.RecordingExpired -> {
                    mBind.txtviewRecordScheduleType.text = "(녹화 종료)"
                    val startTime = state.schedule.startTimestamp
                    val durationMinute = state.schedule.durationMinute
                    if (startTime != null) {
                        mBind.txtviewRecordTime1.text = formatRecordTime1(startTime, durationMinute)
                        mBind.txtviewRecordTime2.text = formatRecordTime2(startTime, durationMinute)
                    }
                }
            }

            mBind.txtviewRecordTime2.isGone = TextUtils.isEmpty(mBind.txtviewRecordTime2.text)
        })
    }

    private fun formatRecordTime1(startTime: Instant, durationMinute: Long): SpannedString {
        val from = startTime.atZone(ZoneId.systemDefault())
        return buildSpannedString {
            append("${from.year}년 ${from.monthValue.twoDigit()}월 ${from.dayOfMonth.twoDigit()}일 ${from.hour.twoDigit()}시 ${from.minute.twoDigit()}분 ${from.second.twoDigit()}초")
            scale(0.8f) {
                color(resColor(R.color.colorDarkText8)) {
                    if (durationMinute <= 0) {
                        append(" 부터 계속")
                    } else {
                        append(" 부터")
                    }
                }
            }
        }
    }

    private fun formatRecordTime2(startTime: Instant, durationMinute: Long): SpannedString? {
        val from = startTime.atZone(ZoneId.systemDefault())
        if (durationMinute <= 0) {
            return null
        }

        val to = from.plusMinutes(durationMinute)
        return when {
            from.year != to.year -> {
                buildSpannedString {
                    append("${to.year}년 ${to.monthValue.twoDigit()}월 ${to.dayOfMonth.twoDigit()}일 ${to.hour.twoDigit()}시 ${to.minute.twoDigit()}분 ${to.second.twoDigit()}초")
                    scale(0.8f) {
                        color(resColor(R.color.colorDarkText8)) { append(" 까지") }
                    }
                }
            }
            from.monthValue != to.monthValue -> {
                buildSpannedString {
                    color(resColor(R.color.colorLightText8)) {
                        append("${to.year}년 ")
                    }
                    append("${to.monthValue.twoDigit()}월 ${to.dayOfMonth.twoDigit()}일 ${to.hour.twoDigit()}시 ${to.minute.twoDigit()}분 ${to.second.twoDigit()}초")
                    scale(0.8f) {
                        color(resColor(R.color.colorDarkText8)) { append(" 까지") }
                    }
                }
            }
            from.dayOfMonth != to.dayOfMonth -> {
                buildSpannedString {
                    color(resColor(R.color.colorLightText8)) {
                        append("${to.year}년 ${to.monthValue.twoDigit()}월 ")
                    }
                    append("${to.dayOfMonth.twoDigit()}일 ${to.hour.twoDigit()}시 ${to.minute.twoDigit()}분 ${to.second.twoDigit()}초")
                    scale(0.8f) {
                        color(resColor(R.color.colorDarkText8)) { append(" 까지") }
                    }
                }
            }
            from.hour != to.hour -> {
                buildSpannedString {
                    color(resColor(R.color.colorLightText8)) {
                        append("${to.year}년 ${to.monthValue.twoDigit()}월 ${to.dayOfMonth.twoDigit()}일 ")
                    }
                    append("${to.hour.twoDigit()}시 ${to.minute.twoDigit()}분 ${to.second.twoDigit()}초")
                    scale(0.8f) {
                        color(resColor(R.color.colorDarkText8)) { append(" 까지") }
                    }
                }
            }
            else -> {
                buildSpannedString {
                    color(resColor(R.color.colorLightText8)) {
                        append("${to.year}년 ${to.monthValue.twoDigit()}월 ${to.dayOfMonth.twoDigit()}일 ${to.hour.twoDigit()}시 ")
                    }
                    append("${to.minute.twoDigit()}분 ${to.second.twoDigit()}초")
                    scale(0.8f) {
                        color(resColor(R.color.colorDarkText8)) { append(" 까지") }
                    }
                }
            }
        }


    }

    private fun openNetworkMediaSetting() {
        val cfg = mViewModel.camManager.config ?: return
        CameraDialogs.openNetworkMedia(
            fm = childFragmentManager,
            media = cfg.enabledNetworkMedia,
        )
    }

    private fun openRecordQuality() {
        val cfg = mViewModel.camManager.config ?: return

        CameraDialogs.openStreamQuality(
            fm = childFragmentManager,
            videoQualityKind = VideoQualityKind.RECORDING,
            title = "녹화 품질",
            fps = cfg.recording.fps,
            resolution = cfg.recording.resolution,
            availableResolutions = cfg.recordingResolutions
        )
    }

    private fun openStreamQuality() {
        val cfg = mViewModel.camManager.config ?: return
        CameraDialogs.openStreamQuality(
            fm = childFragmentManager,
            videoQualityKind = VideoQualityKind.STREAMING,
            title = "스트리밍 품질",
            fps = cfg.streaming.fps,
            resolution = cfg.streaming.resolution,
            availableResolutions = cfg.streamingResolutions
        )
    }

    private fun openCameraNameEdit() {
        val cfg = mViewModel.camManager.config ?: return
        CameraDialogs.openCameraNameEdit(
            fm = childFragmentManager,
            cameraName = cfg.cameraName ?: "",
        )
    }

    private fun openCameraWifiEdit() {
        val cfg = mViewModel.camManager.config ?: return
        CameraDialogs.openCameraWifiEdit(
            fm = childFragmentManager,
            wifiSsid = cfg.wifiSsid,
            wifiPw = cfg.wifiPw,
        )
    }

    private fun openCameraPwEdit() {
        CameraDialogs.openCameraPasswordEdit(fm = childFragmentManager)
    }

    private fun openReboot() {
        val lastCameraIp = mViewModel.camManager.cameraIp ?: return

        // 재부팅하는 동안 연결끊김 메시지 비활성화
        mViewModel.camManager.disconnectedMessage.addDisableRequest()

        CameraDialogs.openReboot(fm = childFragmentManager) { rebooted ->
            if (rebooted) {
                openLogin(lastCameraIp = lastCameraIp)
            } else {
                mViewModel.camManager.disconnectedMessage.removeDisableRequest()
            }
        }
    }


    private fun openLogin(lastCameraIp: String) {
        lifecycleScope.launch {
            CameraDialogs.openLoginNetworkChoose(fm = childFragmentManager) { net ->
                when (net) {
                    "wifi" -> {
                        CameraDialogs.openLoginWifi(fm = childFragmentManager) {
                            removeDisconnectMessageDisableRequest(500)
                        }
                    }
                    "lte" -> {
                        CameraDialogs.openLoginLte(fm = childFragmentManager, cameraIp = lastCameraIp) {
                            removeDisconnectMessageDisableRequest(500)
                        }
                    }
                    else -> {
                        // 이런 경우는 없지만
                        removeDisconnectMessageDisableRequest()
                    }
                }
            }
        }
    }

    private fun removeDisconnectMessageDisableRequest(sleepTime: Long = 0L) {
        lifecycleScope.launch {
            if (sleepTime == 0L) {
                mViewModel.camManager.disconnectedMessage.removeDisableRequest()
            } else {
                delay(sleepTime)
                if (isActive) {
                    mViewModel.camManager.disconnectedMessage.removeDisableRequest()
                }
            }
        }
    }

    /**
     * 녹화 설정 다이얼로그
     */
    private fun openRecordingSchedule() {
        val schedule = mViewModel.camManager.config?.recordingSchedule ?: return
        CameraDialogs.openRecordingSchedule(
            fm = childFragmentManager,
            disabled = schedule.disabled,
            startTime = schedule.startTimestamp ?: Instant.now(),
            durationMinute = schedule.durationMinute
        ) {}
    }
}
