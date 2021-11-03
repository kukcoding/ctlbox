package kr.ohlab.android.recyclerviewgroup

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.TypedValue
import android.view.Display
import android.view.View
import android.view.WindowManager
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kotlin.math.ceil

open class TRViewHolder<T : ViewBinding>(
    val binding: T
) : RecyclerView.ViewHolder(binding.root) {

    val context: Context?
        get() = this.itemView.context

    var holderTracker: HolderTracker? = null

    fun isHolderSelected(): Boolean {
        val check = this.holderTracker ?: return false
        val item = this.item ?: return false
        return check.holderSelected(item)
    }

    fun isHolderSelectable(): Boolean {
        val check = this.holderTracker ?: return false
        val item = this.item ?: return false
        return check.holderSelectable(item)
    }

    fun isHolderEditing(): Boolean {
        val check = this.holderTracker ?: return false
        val item = this.item ?: return false
        return check.holderEditing(item)
    }

    fun isHolderChecked(): Boolean {
        val check = this.holderTracker ?: return false
        val item = this.item ?: return false
        return check.holderChecked(item)
    }


    /**
     * HolderViewClickListener Debounce Click listener
     */
    class HolderClickListener<ITEM : ItemBase<*>>(
        private val holder: TRViewHolder<*>,
        private var action: (View, ITEM) -> Unit
    ) : View.OnClickListener {
        private var lastClickedTime = 0L
        override fun onClick(v: View) {
            val now = System.currentTimeMillis()
            if (now - lastClickedTime < 400L) {
                // Timber.d("onClick: ${now - lastClickedTime}")
                lastClickedTime = now
                return
            }
            lastClickedTime = now
            action.invoke(v, holder.item as ITEM)
        }
    }

    class HolderLongClickListener<ITEM : ItemBase<*>>(
        private val holder: TRViewHolder<*>,
        private var action: (View, ITEM) -> Boolean
    ) : View.OnLongClickListener {

        override fun onLongClick(v: View?): Boolean {
            val view = v ?: return false
            return action.invoke(view, holder.item as ITEM)
        }
    }


    fun <ITEM : ItemBase<*>> registerClickListener(view: View, action: ((View, ITEM) -> Unit)?) {
        if (action == null) {
            view.setOnClickListener(null)
            view.tag = null
        } else {
            val listener = HolderClickListener(this, action)
            view.setOnClickListener(listener)
            view.tag = this
        }
    }

    fun <ITEM : ItemBase<*>> registerClickListener(action: ((View, ITEM) -> Unit)?) {
        registerClickListener(itemView, action)
    }


    fun <ITEM : ItemBase<*>> registerLongClickListener(view: View, action: ((View, ITEM) -> Boolean)?) {
        if (action == null) {
            view.setOnLongClickListener(null)
            view.tag = null
        } else {
            val listener = HolderLongClickListener(this, action)
            view.setOnLongClickListener(listener)
            view.tag = this
        }
    }

    fun <ITEM : ItemBase<*>> registerLongClickListener(action: ((View, ITEM) -> Boolean)?) {
        registerLongClickListener(itemView, action)
    }

    open fun bindClickListener(view: View, listener: ((View) -> Unit)?) {
        view.setOnClickListener(listener)
        if (listener != null) {
            view.tag = this
        }
    }

    open fun bindClickListener(view: View, listener: View.OnClickListener?) {
        view.setOnClickListener(listener)
        if (listener != null) {
            view.tag = this
        } else {
            view.tag = null
        }
    }

    open fun bindLongClickListener(view: View, listener: View.OnLongClickListener?) {
        view.setOnLongClickListener(listener)
        if (listener != null) {
            view.tag = this
        } else {
            view.tag = null
        }
    }

    open fun bindLongClickListener(view: View, listener: ((View) -> Boolean)?) {
        view.setOnLongClickListener(listener)
        if (listener != null) {
            view.tag = this
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    private fun screenSize(context: Context): Point {
        val display = displayCompat(context)!!

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics.bounds
            Point(bounds.width(), bounds.height())
        } else {
            val point = Point()
            display.getSize(point)
            point
        }
    }

    protected fun screenWidth(): Int {
        val ctx = context ?: return 800
        return screenSize(ctx).x
    }

    @ColorInt
    @Deprecated("use resColor", ReplaceWith("resColor(resId)"))
    fun resourceColor(@ColorRes resId: Int): Int = resColor(resId)

    @ColorInt
    @Deprecated("use styledColor", ReplaceWith("styledColor(attrRes)"))
    fun themeColor(@AttrRes attrRes: Int): Int = styledColor(attrRes)

    @ColorInt
    fun resColor(@ColorRes resId: Int): Int =
        ContextCompat.getColor(this.context!!, resId)

    @ColorInt
    fun styledColor(@AttrRes attrRes: Int): Int {
        val theme = this.context!!.theme
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    private fun displayCompat(context: Context): Display? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display
        } else {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        }
    }

    data class SpanInfo(var totalSpanCount: Int = -1, var spanCount: Int = -1, var parentViewWidth: Int = -1) {
        val viewWidth: Int?
            get() {
                if (totalSpanCount < 1 || spanCount < 1 || parentViewWidth < 1) return null
                return ceil(parentViewWidth * (spanCount.toFloat() / totalSpanCount)).toInt()
            }
    }

    val span = SpanInfo()

    fun updateParentSpanInfo(parentViewWidth: Int, totalSpanCount: Int) {
        span.parentViewWidth = parentViewWidth
        span.totalSpanCount = totalSpanCount
    }

    fun updateSpanCount(spanCount: Int) {
        span.spanCount = spanCount
    }

    fun bind(item: ItemBase<T>) {
        this.item = item
    }

    fun unbind() {
        item = null
    }


    val extras: MutableMap<String, Any>
        get() = item!!.extras
    val swipeDirs: Int
        get() = item!!.swipeDirs
    val dragDirs: Int
        get() = item!!.dragDirs

    var item: ItemBase<T>? = null

    val root: View
        get() = itemView


}

