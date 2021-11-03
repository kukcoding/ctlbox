package myapp.ui.player.vlc.view

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import myapp.extensions.dp
import myapp.ui.player.R

class LegendView : ConstraintLayout, PlotViewDataChangeListener {

    private var plotViewId: Int = -1
    private lateinit var plotView: PlotView

    constructor(context: Context) : super(context) {
        setWillNotDraw(false)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttributes(attrs, 0)
        setWillNotDraw(false)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initAttributes(attrs, defStyle)
        setWillNotDraw(false)
    }

    private fun initAttributes(attrs: AttributeSet, defStyle: Int) {
        attrs.let {

            val a = context.theme.obtainStyledAttributes(attrs, R.styleable.LPGPlotView, 0, defStyle)
            try {
                plotViewId = a.getResourceId(a.getIndex(R.styleable.LPGLegendView_lpg_plot_view), -1)
            } catch (e: Exception) {
                Log.w("", e.message, e)
            } finally {
                a.recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //workaround for editor
        if (!isInEditMode) {
            context?.let {
                plotView = (it as Activity).findViewById(plotViewId)
                if (!::plotView.isInitialized) throw IllegalStateException("A valid plot view has to be provided")
                plotView.addListener(this)
            }
        }
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
    }

    override fun onDataChanged(data: List<Pair<LineGraph, String>>) {
        removeAllViews()
        val grid = GridLayout(context)
        grid.columnCount = 2
        addView(grid)
        data.forEach {
            val title = TextView(context)
            title.text = it.first.title
            title.setTextColor(it.first.color)
            grid.addView(title)

            val value = TextView(context)
            value.text = it.second
            val layoutParams = GridLayout.LayoutParams()
            layoutParams.leftMargin = 4.dp
            value.layoutParams = layoutParams
            grid.addView(value)
        }
    }
}
