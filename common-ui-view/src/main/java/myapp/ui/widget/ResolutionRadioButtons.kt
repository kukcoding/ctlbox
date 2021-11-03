package myapp.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import com.google.android.flexbox.FlexboxLayout
import myapp.ui.common.R
import myapp.ui.common.databinding.WidgetResolutionRadioButtonsBinding

class ResolutionRadioButtons @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    private var mBind = WidgetResolutionRadioButtonsBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    interface OnSelectListener {
        fun onSelect(resolution: String)
    }

    var resolutions: List<String> = emptyList()
        set(newValue) {
            if (field == newValue) {
                return
            }

            field = newValue
            resetItemViews()
        }

    var selectedResolution: String? = null
        set(newValue) {
            if (field == newValue) {
                return
            }

            field = newValue
            resetItemViews()
        }

    init {
        setOnClickListener {
            // dummy, ignore background click
        }
        customInit(context, attrs, defStyleAttr)
    }

    private fun customInit(context: Context, attrs: AttributeSet?, defStyle: Int) {
        if (isInEditMode) {
            this.resolutions = listOf("1920x1080", "1280x720", "640x480")
        }
        resetItemViews()
    }

    private fun resetItemViews() {
        // view와 데이터의 개수를 맞추고
        prepareItemViews()
        layoutItems()
    }

    private fun prepareItemViews() {
        val needChildCount = this.resolutions.size
        var li: LayoutInflater? = null
        while (mBind.flexbox.childCount < needChildCount) {
            if (li == null) li = LayoutInflater.from(context)
            val itemView = li!!.inflate(R.layout.common_resolution_radio_button, null, false) as ViewGroup

            mBind.flexbox.addView(
                itemView,
                FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            )
        }
    }

    private fun layoutItems() {
        // val childViewCount = mBind.flexbox.childCount
        // if (childViewCount >= resolutions.size) return

        mBind.flexbox.children.forEachIndexed { index, view ->
            val itemView = view as ResolutionRadioButton
            itemView.setOnClickListener(itemClickListener)
            if (index >= this.resolutions.size) {
                itemView.resolution = null
                itemView.tag = null
                itemView.isVisible = false
            } else {
                itemView.isVisible = true
                val r = resolutions[index]
                itemView.resolution = r
                itemView.isSelected = r == selectedResolution
            }
        }
    }

    private val itemClickListener = OnClickListener { v ->
        val resolution = (v as ResolutionRadioButton).resolution ?: return@OnClickListener
        onSelectListener?.onSelect(resolution)
    }

    var onSelectListener: OnSelectListener? = null
}
