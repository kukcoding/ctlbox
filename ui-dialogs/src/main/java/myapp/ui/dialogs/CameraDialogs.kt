package myapp.ui.dialogs

import androidx.fragment.app.FragmentManager
import myapp.data.entities.RecordFileFilter
import myapp.ui.dialogs.cameraname.CameraNameEditDialogFragment
import myapp.ui.dialogs.camerapassword.CameraPasswordEditDialogFragment
import myapp.ui.dialogs.loginlte.LoginLteDialogFragment
import myapp.ui.dialogs.loginmediachoose.LoginNetworkChooseDialogFragment
import myapp.ui.dialogs.loginwifi.LoginWifiDialogFragment
import myapp.ui.dialogs.networkmedia.NetworkMediaDialogFragment
import myapp.ui.dialogs.record.RecordFileFiltersDialogFragment
import myapp.ui.dialogs.streamquality.StreamQuality
import myapp.ui.dialogs.streamquality.StreamQualityDialogFragment
import myapp.ui.dialogs.wifi.CameraWifiEditDialogFragment
import myapp.util.Action1

object CameraDialogs {

    fun openLoginNetworkChoose(
        fm: FragmentManager,
        onDismiss: Action1<String?>
    ) = LoginNetworkChooseDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.show(fm, null)
    }

    fun openLoginWifi(
        fm: FragmentManager,
        onDismiss: Action1<Boolean>? = null
    ) = LoginWifiDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.show(fm, null)
    }


    fun openLoginLte(
        fm: FragmentManager,
        cameraIp: String?,
        onDismiss: Action1<Boolean>? = null
    ) = LoginLteDialogFragment.newInstance(cameraIp = cameraIp).apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.show(fm, null)
    }

    fun openRecordFileFilters(
        fm: FragmentManager,
        recordFileFilters: List<RecordFileFilter>,
        onDismiss: Action1<RecordFileFilter?>
    ) = RecordFileFiltersDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.mArgRecordFileFilters = recordFileFilters
        it.show(fm, null)
    }


    fun openStreamQuality(
        fm: FragmentManager,
        title: String,
        streamQuality: StreamQuality,
        onDismiss: Action1<StreamQuality?>
    ) = StreamQualityDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.mArgTitle = title
        it.mArgStreamQuality = streamQuality
        it.show(fm, null)
    }

    fun openNetworkMedia(
        fm: FragmentManager,
        media: String,
        onDismiss: Action1<String?>
    ) = NetworkMediaDialogFragment.newInstance(media = media).apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.show(fm, null)
    }


    fun openCameraNameEdit(
        fm: FragmentManager,
        cameraName: String,
        onDismiss: Action1<String?>
    ) = CameraNameEditDialogFragment.newInstance(cameraName = cameraName).apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.show(fm, null)
    }


    fun openCameraPasswordEdit(
        fm: FragmentManager,
        onDismiss: Action1<Boolean>
    ) = CameraPasswordEditDialogFragment.newInstance().apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.show(fm, null)
    }

    fun openCameraWifiEdit(
        fm: FragmentManager,
        ssid: String,
        onDismiss: Action1<String?>
    ) = CameraWifiEditDialogFragment.newInstance(ssid = ssid).apply {
        isCancelable = true
        onDismissListener = onDismiss
    }.also {
        it.show(fm, null)
    }
}
