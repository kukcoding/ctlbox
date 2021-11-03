package myapp.ui.listeners

import android.view.View


open class ContinuousClickCountListener(private val validClickInterval: Long = 1000L) : View.OnClickListener {
    private var mLastClickTime: Long = 0L
    private var mClickCount: Int = 0

    override fun onClick(v: View?) {
        val now = System.currentTimeMillis()
        val diff = now - mLastClickTime
        mLastClickTime = now
        if (diff > validClickInterval) {
            mClickCount = 1
        } else {
            mClickCount++
        }

        val handled = onClickCountChanged(mClickCount)
        if (handled) {
            mClickCount = 0
            mLastClickTime = 0L
        }
    }

    open fun onClickCountChanged(count: Int): Boolean {
        return false
    }
}
