package kr.ohlab.android.recyclerviewgroup

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.viewbinding.ViewBinding
import kr.ohlab.android.recyclerviewgroup.support.SimpleViewTypeBinderCreator
import kr.ohlab.android.recyclerviewgroup.support.ViewTypeBinderCreator
import kr.ohlab.android.recyclerviewgroup.support.ViewTypeConfig
import kr.ohlab.android.recyclerviewgroup.support.ViewTypeResolver
import kotlin.reflect.KClass


open class TRListAdapter(
    val totalSpanCount: Int,
    internal var viewTypeResolver: ViewTypeResolver,
    internal var viewTypeConfigsMap: Map<Int, ViewTypeConfig>,
    diffCallback: DiffUtil.ItemCallback<ItemBase<*>> = DiffItemCallback()
) : ListAdapter<ItemBase<*>, TRViewHolder<*>>(diffCallback) {

    private val callbacksOnBindBefore: MutableMap<KClass<out ItemBase<*>>, (ItemBase<*>) -> Unit> =
        mutableMapOf()

    private val callbacksOnBindAfter: MutableMap<KClass<out ItemBase<*>>, (ItemBase<*>) -> Unit> =
        mutableMapOf()

    private val callbacksOnRecycledBefore: MutableMap<KClass<out ItemBase<*>>, (ItemBase<*>) -> Unit> =
        mutableMapOf()

    // private var viewTypeConfigsMap: Map<Int, ViewTypeConfig> = viewTypeConfigs.map { it.viewType to it }.toMap()

    val bindingItemCreator: ViewTypeBinderCreator by lazy {
        SimpleViewTypeBinderCreator {
            this.viewTypeConfigsMap[it]?.binderClass
                ?: throw IllegalStateException("cannot find ViewTypeConfigs for viewType:$it")
        }
    }

    var holderTracker: HolderTracker? = null

    fun <BINDING : ViewBinding, ITEM : ItemBase<BINDING>> registerOnBindBefore(
        clazz: KClass<out ITEM>,
        callback: (ITEM) -> Unit
    ): TRListAdapter {
        @Suppress("UNCHECKED_CAST")
        callbacksOnBindBefore[clazz] = callback as (ItemBase<*>) -> Unit
        return this
    }

    fun <BINDING : ViewBinding, ITEM : ItemBase<BINDING>> registerOnBindAfter(
        clazz: KClass<out ITEM>,
        callback: (ITEM) -> Unit
    ): TRListAdapter {
        @Suppress("UNCHECKED_CAST")
        callbacksOnBindAfter[clazz] = callback as (ItemBase<*>) -> Unit
        return this
    }


    fun <ITEM : ItemBase<*>> registerOnRecycledBefore(
        clazz: KClass<out ITEM>,
        callback: (ITEM) -> Unit
    ): TRListAdapter {
        @Suppress("UNCHECKED_CAST")
        callbacksOnRecycledBefore[clazz] = callback as (ItemBase<*>) -> Unit
        return this
    }

    fun getSpanSizeAt(position: Int): Int {
        if (position >= itemCount) return 0
        val data = getItem(position) ?: return 0
        val viewType = getItemViewType(position)
//        Timber.d(
//            "pos: %d, viewType: %d spanSize=${viewTypeConfigsMap[viewType]?.spanSize}",
//            position,
//            viewType
//        )
        return viewTypeConfigsMap[viewType]?.spanSize ?: 0
    }


    private fun createItemBaseByViewType(parentView: ViewGroup, viewType: Int): ViewBinding {
        return bindingItemCreator.create(parentView, viewType)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TRViewHolder<*> {
        val viewBinding = createItemBaseByViewType(parent, viewType)
        val holder = TRViewHolder(viewBinding)
        holder.updateParentSpanInfo(parentViewWidth = parent.measuredWidth, totalSpanCount = totalSpanCount)
        return holder
    }

    override fun onBindViewHolder(holder: TRViewHolder<*>, position: Int) {
        // Never called (all binds go through the version with payload)
    }

    override fun onBindViewHolder(holder: TRViewHolder<*>, position: Int, payloads: List<Any>) {
        val item = getItem(position)
        holder.holderTracker = this.holderTracker
        holder.updateSpanCount(getSpanSizeAt(position))
        callbacksOnBindBefore[item::class]?.invoke(item)
        item.bind(holder, position, payloads)
        callbacksOnBindAfter[item::class]?.invoke(item)
    }

    override fun onViewRecycled(holder: TRViewHolder<*>) {
        val item = holder.item ?: return
        callbacksOnRecycledBefore[item::class]?.invoke(item)
        item.unbind(holder)
    }

    override fun onFailedToRecycleView(holder: TRViewHolder<*>): Boolean {
        return holder.item?.isRecyclable ?: false
    }

    override fun onViewAttachedToWindow(holder: TRViewHolder<*>) {
        super.onViewAttachedToWindow(holder)
        holder.item?.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: TRViewHolder<*>) {
        super.onViewDetachedFromWindow(holder)
        holder.item?.onViewDetachedFromWindow(holder)
    }

    override fun getItemViewType(position: Int): Int {
        val lastItem = getItem(position) ?: throw RuntimeException("Invalid position $position")
        return viewTypeResolver.resolve(position, lastItem)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    fun getItemAt(position: Int): ItemBase<*> {
        return super.getItem(position)
    }

    fun getItemOrNull(position: Int): ItemBase<*>? {
        return if (position >= 0 && position < this.itemCount) {
            super.getItem(position)
        } else {
            null
        }

    }

    fun lastOrNull() = if (this.itemCount > 0) this.getItem(this.itemCount - 1) else null
    fun firstOrNull() = if (this.itemCount > 0) this.getItem(0) else null
}
