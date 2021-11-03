package myapp.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import myapp.ui.common.R
import myapp.ui.common.databinding.WidgetResolutionRadioButtonBinding

class ResolutionRadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var mBind = WidgetResolutionRadioButtonBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    var resolution: String? = null
        set(newValue) {
            if (field == newValue) {
                return
            }

            field = newValue
            updateUi()
        }


    init {
        orientation = HORIZONTAL
        customInit(context, attrs, defStyleAttr)
    }

    private fun customInit(context: Context, attrs: AttributeSet?, defStyle: Int) {
        //Log.d(TAG, "XXX customInit")
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ResolutionRadioButton,
            defStyle,
            0
        )
        this.resolution = a.getString(R.styleable.ResolutionRadioButton_resolution)
        a.recycle()
        updateUi()
    }

    private fun updateUi() {
        val empty = this.resolution.isNullOrBlank()
        this.isVisible = !empty
        if (!empty) {
            mBind.txtviewResolution.text = this.resolution
        }
    }
}
