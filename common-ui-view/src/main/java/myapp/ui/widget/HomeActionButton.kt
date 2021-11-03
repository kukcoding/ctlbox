package myapp.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import myapp.ui.common.R
import myapp.ui.common.databinding.WidgetHomeActionButtonBinding

class HomeActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var mBind = WidgetHomeActionButtonBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    var title: String? = "고화질 재생"
        set(newValue) {
            if (field == newValue) {
                return
            }

            field = newValue
            updateUi()
        }


    var icon: Drawable? = null
        set(newValue) {
            if (field == newValue) {
                return
            }

            field = newValue
            updateUi()
        }


    init {
        orientation = HORIZONTAL
        setOnClickListener {
            // dummy, ignore background click
        }
        customInit(context, attrs, defStyleAttr)
    }

    private fun customInit(context: Context, attrs: AttributeSet?, defStyle: Int) {
        //Log.d(TAG, "XXX customInit")
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.HomeActionButton,
            defStyle,
            0
        )
        this.title = a.getString(R.styleable.HomeActionButton_title)
        this.icon = a.getDrawable(R.styleable.HomeActionButton_icon)
        a.recycle()
        updateUi()
    }

    private fun updateUi() {
        mBind.txtviewTitle.text = this.title ?: ""
        mBind.imgviewPlayIcon.setImageDrawable(this.icon)
    }
}
