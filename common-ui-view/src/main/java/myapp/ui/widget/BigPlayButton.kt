package myapp.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isGone
import myapp.ui.common.R
import myapp.ui.common.databinding.WidgetBigPlayButtonBinding

class BigPlayButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var mBind = WidgetBigPlayButtonBinding.inflate(
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

    var subtitle: String? = "RTSP"
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
        mBind.txtviewTitle.text = "고화질 재생 (RTSP)"
        mBind.txtviewSubtitle.text = ""
        customInit(context, attrs, defStyleAttr)
    }

    private fun customInit(context: Context, attrs: AttributeSet?, defStyle: Int) {
        //Log.d(TAG, "XXX customInit")
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.BigPlayButton,
            defStyle,
            0
        )
        this.title = a.getString(R.styleable.BigPlayButton_title)
        this.subtitle = a.getString(R.styleable.BigPlayButton_subtitle)
        a.recycle()
        updateUi()
    }

    private fun updateUi() {
        mBind.txtviewTitle.text = this.title ?: ""
        mBind.txtviewSubtitle.isGone = this.subtitle.isNullOrBlank()
        mBind.txtviewSubtitle.text = this.subtitle ?: ""
    }
}
