package myapp.util

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View


/**
 *
 * @param initialInterval Initial interval in millis
 * @param normalInterval Normal interval in millis
 * @param clickListener The OnClickListener to trigger
 */
class OnRepeatListenerKey(
    private val initialInterval: Int,
    private val normalInterval: Int,
    private val speedUpDelay: Int,
    private val clickListener: View.OnClickListener
) : View.OnKeyListener, OnRepeatListener(initialInterval, normalInterval, speedUpDelay, clickListener) {

    /**
     *
     * @param clickListener The OnClickListener to trigger
     */
    constructor(clickListener: View.OnClickListener) : this(
        DEFAULT_INITIAL_DELAY,
        DEFAULT_NORMAL_DELAY,
        DEFAULT_SPEEDUP_DELAY,
        clickListener
    )

    override fun onKey(view: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (view == null || event == null) return false
        if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (downView != view) startRepeating(view)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopRepeating(view)
                return true
            }
        }
        return false
    }

}
