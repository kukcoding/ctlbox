package myapp.util
import android.os.Message
import android.util.Log
import android.view.View
import myapp.WeakHandler
import myapp.base.BuildConfig


/**
 *
 * @param initialInterval Initial interval in millis
 * @param normalInterval Normal interval in millis
 * @param clickListener The OnClickListener to trigger
 */
open class OnRepeatListener(
    private val initialInterval: Int,
    private val normalInterval: Int,
    private val speedUpDelay: Int,
    private val clickListener: View.OnClickListener
) {
    var downView: View? = null
    var initialTime: Long = -1L
    private val handler = OnRepeatHandler(this)

    init {
        if (initialInterval < 0 || normalInterval < 0)
            throw IllegalArgumentException("negative interval")
    }



    fun startRepeating(view: View) {
        handler.removeMessages(ACTION_ONCLICK)
        handler.sendEmptyMessageDelayed(ACTION_ONCLICK, initialInterval.toLong())
        downView = view
        initialTime = System.currentTimeMillis()
        clickListener.onClick(view)
        view.isPressed = true
        if (BuildConfig.DEBUG) Log.d("Delay", "onTouch: ACTION_DOWN")
    }

    fun stopRepeating(view: View) {
        handler.removeMessages(ACTION_ONCLICK)
        downView = null
        initialTime = -1L
        view.isPressed = false
        if (BuildConfig.DEBUG) Log.d("Delay", "onTouch: ACTION_UP")
    }

    private class OnRepeatHandler(owner: OnRepeatListener) : WeakHandler<OnRepeatListener>(owner) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                ACTION_ONCLICK -> {
                    val interval = if (owner!!.initialTime > -1L && System.currentTimeMillis() - owner!!.initialTime > owner!!.speedUpDelay) owner!!.normalInterval.toLong() / 3 else owner!!.normalInterval.toLong()
                    sendEmptyMessageDelayed(ACTION_ONCLICK, interval)
                    owner!!.clickListener.onClick(owner!!.downView)
                }
            }
        }
    }

    companion object {

        private const val ACTION_ONCLICK = 0

        //Default values in milliseconds
        const val DEFAULT_INITIAL_DELAY = 500
        const val DEFAULT_NORMAL_DELAY = 150
        const val DEFAULT_SPEEDUP_DELAY = 2000
    }
}
