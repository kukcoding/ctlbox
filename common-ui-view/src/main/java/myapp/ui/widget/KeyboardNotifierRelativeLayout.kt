package myapp.ui.widget


import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import myapp.util.AndroidUtils


class KeyboardNotifierRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    var keyboardDelegate: KeyboardNotifierDelegate? = null

    private val rect = Rect()
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        keyboardDelegate?.let {
            val usableViewHeight = rootView.height - AndroidUtils.getStatusBarHeight()
            getWindowVisibleDisplayFrame(rect)
            val keyboardHeight = usableViewHeight - (rect.bottom - rect.top)
            it.onKeyboardHeightChanged(keyboardHeight)
        }

    }

    @SuppressLint("ObsoleteSdkInt")
    fun getViewInset(view: View?): Int {
        if (view == null || Build.VERSION.SDK_INT < 21) {
            return 0
        }
        try {
            val attachInfoField = View::class.java.getDeclaredField("mAttachInfo")
            attachInfoField.isAccessible = true
            val attachInfo = attachInfoField.get(view)
            if (attachInfo != null) {
                val insetsField = attachInfo.javaClass.getDeclaredField("mStableInsets")
                insetsField.isAccessible = true
                val insets = insetsField.get(attachInfo) as Rect
                return insets.bottom
            }
        } catch (e: Exception) {
            Log.d("KeyboardNotiRLayout","getViewInset(view) failure: " + e.message)
        }

        return 0
    }
}
