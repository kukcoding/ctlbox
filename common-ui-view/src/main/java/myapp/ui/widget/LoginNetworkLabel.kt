package myapp.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isGone
import myapp.ui.common.R
import myapp.ui.common.databinding.WidgetLoginNetworkLabelBinding

class LoginNetworkLabel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var mBind = WidgetLoginNetworkLabelBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    var title: String? = "WIFI"
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
        mBind.txtviewTitle.text = "WIFI"
        customInit(context, attrs, defStyleAttr)
    }

    private fun customInit(context: Context, attrs: AttributeSet?, defStyle: Int) {
        //Log.d(TAG, "XXX customInit")
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.LoginNetworkLabel,
            defStyle,
            0
        )
        this.title = a.getString(R.styleable.LoginNetworkLabel_title)
        a.recycle()
        updateUi()
    }

    private fun updateUi() {
        this.isGone = title.isNullOrBlank()
        mBind.txtviewTitle.text = title?.uppercase() ?: ""
    }
}
