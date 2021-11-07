package myapp.ui.dialogs

import androidx.fragment.app.FragmentManager
import myapp.data.code.VideoQualityKind
import myapp.data.entities.RecordFileFilter
import myapp.ui.dialogs.cameraname.CameraNameEditDialogFragment
import myapp.ui.dialogs.camerapassword.CameraPasswordEditDialogFragment
import myapp.ui.dialogs.loginlte.LoginLteDialogFragment
import myapp.ui.dialogs.loginmediachoose.LoginNetworkChooseDialogFragment
import myapp.ui.dialogs.loginwifi.LoginWifiDialogFragment
import myapp.ui.dialogs.networkmedia.NetworkMediaDialogFragment
import myapp.ui.dialogs.reboot.CameraRebootDialogFragment
import myapp.ui.dialogs.recordfiledownload.RecordFileDownloadDialogFragment
import myapp.ui.dialogs.recordfilefilter.RecordFileFiltersDialogFragment
import myapp.ui.dialogs.recordingschedule.RecordingScheduleDialogFragment
import myapp.ui.dialogs.streamquality.StreamQualityDialogFragment
import myapp.ui.dialogs.wifi.CameraWifiEditDialogFragment
import myapp.util.Action1
import org.threeten.bp.Instant

object CameraDialogs {

    fun openLoginNetworkChoose(
        fm: FragmentManager,
        onDismiss: Action1<String?>
    ) = LoginNetworkChooseDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }

    fun openLoginWifi(
        fm: FragmentManager,
        onDismiss: Action1<Boolean>? = null
    ) = LoginWifiDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }


    fun openLoginLte(
        fm: FragmentManager,
        cameraIp: String?,
        onDismiss: Action1<Boolean>? = null
    ) = LoginLteDialogFragment.newInstance(cameraIp = cameraIp).apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }

    fun openRecordFileFilters(
        fm: FragmentManager,
        recordFileFilters: List<RecordFileFilter>,
        onDismiss: Action1<RecordFileFilter?>
    ) = RecordFileFiltersDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.mArgRecordFileFilters = recordFileFilters
        it.show(fm, null)
    }


    fun openStreamQuality(
        fm: FragmentManager,
        videoQualityKind: VideoQualityKind,
        title: String,
        fps: Int,
        resolution: String,
        availableResolutions: List<String>,
        onDismiss: Action1<Boolean>? = null
    ) = StreamQualityDialogFragment.newInstance(
        videoQualityKind = videoQualityKind,
        title = title,
        fps = fps,
        resolution = resolution,
        availableResolutions = availableResolutions
    ).apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }

    fun openNetworkMedia(
        fm: FragmentManager,
        media: String,
        onDismiss: Action1<String?>? = null
    ) = NetworkMediaDialogFragment.newInstance(media = media).apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }


    fun openCameraNameEdit(
        fm: FragmentManager,
        cameraName: String,
        onDismiss: Action1<String?>? = null
    ) = CameraNameEditDialogFragment.newInstance(cameraName = cameraName).apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }


    fun openCameraPasswordEdit(
        fm: FragmentManager,
        onDismiss: Action1<Boolean>? = null
    ) = CameraPasswordEditDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }

    fun openCameraWifiEdit(
        fm: FragmentManager,
        wifiSsid: String?,
        wifiPw: String?,
        onDismiss: Action1<String?>? = null
    ) = CameraWifiEditDialogFragment.newInstance(wifiSsid = wifiSsid, wifiPw = wifiPw).apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }

    fun openRecordFileDownload(
        fm: FragmentManager,
        fileName: String,
        autoCloseDelay: Long,
        onDismiss: Action1<String?>? = null
    ) = RecordFileDownloadDialogFragment.newInstance(
        fileId = fileName,
        autoCloseDelay = autoCloseDelay
    ).apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }


    fun openRecordFileDownload(
        fm: FragmentManager,
        fileId: String,
        onDismiss: Action1<String?>? = null
    ) = openRecordFileDownload(
        fm = fm,
        fileName = fileId,
        autoCloseDelay = 10_000L,
        onDismiss = onDismiss
    )


    fun openReboot(
        fm: FragmentManager,
        onDismiss: Action1<Boolean>
    ) = CameraRebootDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.show(fm, null)
    }

    fun openRecordingSchedule(
        fm: FragmentManager,
        disabled: Boolean,
        startTime: Instant,
        durationMinute: Long,
        onDismiss: Action1<Boolean>?
    ) = RecordingScheduleDialogFragment.newInstance(
        disabled = disabled,
        startTime = startTime,
        durationMinute = durationMinute
    ).apply {
        isCancelable = true
        onDismissListener = onDismiss ?: {}
    }.also {
        it.show(fm, null)
    }
}
