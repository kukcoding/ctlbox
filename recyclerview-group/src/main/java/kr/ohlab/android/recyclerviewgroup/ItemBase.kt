package kr.ohlab.android.recyclerviewgroup

import android.content.Context
import android.util.TypedValue
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicLong


abstract class ItemBase<T : ViewBinding> protected constructor(
    id: Long? = null
) {
    open val id: Long = id ?: ID_COUNTER.decrementAndGet()
    private var viewHolderRef: WeakReference<TRViewHolder<T>> = WeakReference(null)
    var viewHolder: TRViewHolder<T>?
        get() = viewHolderRef.get()
        set(newValue) {
            viewHolderRef = WeakReference(newValue)
        }

    val isHolderSelected: Boolean
        get() = viewHolder?.isHolderSelected() ?: false


    val isHolderEditing: Boolean
        get() = viewHolder?.isHolderEditing() ?: false


    val context: Context?
        get() = this.viewHolder?.context


    fun resStr(@StringRes resId: Int): String = this.context!!.resources.getString(resId)

    @ColorInt
    fun resColor(@ColorRes resId: Int): Int = ContextCompat.getColor(this.context!!, resId)

    @ColorInt
    fun styledColor(@AttrRes attrRes: Int): Int {
        val theme = this.context!!.theme
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }

    val extras = mutableMapOf<String, Any>()

    open fun bind(viewHolder: TRViewHolder<*>, position: Int, payloads: List<Any>) {
        @Suppress("UNCHECKED_CAST")
        val holder = viewHolder as TRViewHolder<T>
        holder.item = this
        this.viewHolder = holder

        onBindBefore(holder)
        onBind(holder, position, payloads)
    }


    /**
     * Do any cleanup required for the viewholder to be reused.
     *
     * @param viewHolder The ViewHolder being recycled
     */
    @CallSuper
    fun unbind(viewHolder: TRViewHolder<*>) {
        onUnbind(viewHolder as TRViewHolder<T>)
        //this.viewHolder = null
        viewHolder.unbind()
    }

    open fun onBindBefore(holder: TRViewHolder<T>) {
    }

    abstract fun onBind(holder: TRViewHolder<T>, position: Int)

    open fun onBind(holder: TRViewHolder<T>, position: Int, payloads: List<Any>) {
        onBind(holder, position)
    }


    open fun onUnbind(holder: TRViewHolder<T>) {
    }


    /**
     * Whether the view should be recycled. Return false to prevent the view from being recycled.
     * (Note that it may still be re-bound.)
     *
     * @return Whether the view should be recycled.
     * @see RecyclerView.Adapter.onFailedToRecycleView
     */
    val isRecyclable: Boolean
        get() = true

    val swipeDirs: Int
        get() = 0
    val dragDirs: Int
        get() = 0

    fun onViewAttachedToWindow(viewHolder: TRViewHolder<*>) {}
    fun onViewDetachedFromWindow(viewHolder: TRViewHolder<*>) {}

    open val isClickable: Boolean
        get() = true
    open val isLongClickable: Boolean
        get() = true


    fun isSameAs(other: ItemBase<*>): Boolean {
        // check runtime class
        // return this.id == other.id && other::class.java == this::class.java
        return this.id == other.id
//        return if (viewType != other.viewType) {
//            false
//        } else id == other.id
    }


    fun hasSameContentAs(other: ItemBase<*>): Boolean {
        return this == other
    }

    fun getChangePayload(newItem: ItemBase<*>): Any? {
        return null
    }


    companion object {
        private val ID_COUNTER = AtomicLong(0)
    }
}
