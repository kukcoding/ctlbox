package myapp.ui.viewpager


import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class SafeViewPager : ViewPager {

    var isScrolled = true
        private set

    constructor(context: Context) : super(context) {
        setScrollStateListener()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setScrollStateListener()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            false
        }

    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return try {
            super.onTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
            false
        }
    }

    private fun setScrollStateListener() {
        addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageScrollStateChanged(state: Int) {
                isScrolled = state == SCROLL_STATE_IDLE
            }
        })
    }

}
