package myapp.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.MenuRes
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.PopupMenu
import myapp.ui.common.R

class PopupMenuButton : AppCompatImageButton {
    constructor(context: Context) : this(context, null)
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, android.R.attr.actionOverflowButtonStyle)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PopupMenuButton)
        val menuRes = a.getResourceId(R.styleable.PopupMenuButton_menu, -1)
        if (menuRes != -1) {
            inflateMenu(menuRes)
        }
        a.recycle()
    }

    val popupMenu: PopupMenu = InternalPopupMenu(context, this, R.attr.actionOverflowMenuStyle)
    var popupMenuListener: PopupMenuListener? = null

    fun inflateMenu(@MenuRes menuRes: Int) = popupMenu.inflate(menuRes)

    fun setMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener?) {
        popupMenu.setOnMenuItemClickListener(listener)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (popupMenu.dragToOpenListener.onTouch(this, event)) {
            return true
        }
        return super.dispatchTouchEvent(event)
    }

    override fun performClick(): Boolean {
        popupMenu.show()
        return true
    }

    interface PopupMenuListener {
        fun onPreparePopupMenu(popupMenu: PopupMenu)
    }

    private fun onPrePopupShow() {
        popupMenuListener?.also {
            it.onPreparePopupMenu(popupMenu)
        }
    }

    private inner class InternalPopupMenu(
        context: Context,
        anchor: View,
        gravity: Int = Gravity.NO_GRAVITY,
        @AttrRes popupStyleAttr: Int = R.attr.popupMenuStyle,
        @StyleRes popupStyleRes: Int = 0
    ) : PopupMenu(context, anchor, gravity, popupStyleAttr, popupStyleRes) {
        override fun show() {
            onPrePopupShow()
            super.show()
        }
    }
}
