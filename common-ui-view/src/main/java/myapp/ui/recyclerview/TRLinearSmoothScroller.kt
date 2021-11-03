package myapp.ui.recyclerview

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller

class TRLinearSmoothScroller(
    private val context: Context,
    private val snapPreference: Int = SNAP_TO_ANY,
    private val scrollMsPerInch: Float = 25f
) : LinearSmoothScroller(context) {

    var targetOffset: Int = 0

    override fun getVerticalSnapPreference(): Int = snapPreference

    override fun getHorizontalSnapPreference(): Int = snapPreference

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
        return scrollMsPerInch / displayMetrics.densityDpi
    }

    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        return targetOffset + super.calculateDtToFit(
            viewStart,
            viewEnd,
            boxStart,
            boxEnd,
            snapPreference
        )
    }
}
