package myapp.ui.recyclerview

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller

class VerticalCenterSmoothScroller constructor(context: Context) : LinearSmoothScroller(context) {

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        // 작은 값을 리턴할 수록 더 빠르다(0.1과 0.2는 너무 빨라서 그런지.. 제대로 동작안함)
        return super.calculateSpeedPerPixel(displayMetrics) * 0.3f
    }

    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        return boxStart + (boxEnd - boxStart) / 2 - (viewStart + (viewEnd - viewStart) / 2)
    }
}
