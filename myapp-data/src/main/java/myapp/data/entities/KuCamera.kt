package myapp.data.entities

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import myapp.BuildVars

@Parcelize
data class KuCamera(
    val cameraId: String,

    val cameraName: String?,

    val lastIp: String?,

    val lastConnectTimestamp: Long,
) : Parcelable {
    @IgnoredOnParcel
    val isWifi = this.lastIp == BuildVars.cameraAccessPointIp

    @IgnoredOnParcel
    val isLte = !isWifi
}
