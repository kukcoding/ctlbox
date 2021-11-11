package myapp.data.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class KuCamera(
    val cameraId: String,

    val cameraName: String?,

    val lastIp: String?,

    val lastConnectTimestamp: Long,
) : Parcelable
