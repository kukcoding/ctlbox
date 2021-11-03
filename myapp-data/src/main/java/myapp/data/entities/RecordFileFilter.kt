package myapp.data.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.parcelize.IgnoredOnParcel
import myapp.extensions.twoDigit

@Parcelize
data class RecordFileFilter(
    val year: Int,
    val monthValue: Int,
    val dayOfMonth: Int,
    val hour: Int,
    val count: Int
) : Parcelable {

    @IgnoredOnParcel
    val key: String
        get() = "${year}${monthValue.twoDigit()}${dayOfMonth.twoDigit()}${hour.twoDigit()}"
}
