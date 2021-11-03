package kr.ohlab.android.recyclerviewgroup

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kr.ohlab.android.recyclerviewgroup.support.SimpleViewTypeBinderCreator
import kr.ohlab.android.recyclerviewgroup.support.ViewTypeBinderCreator
import kr.ohlab.android.recyclerviewgroup.support.ViewTypeConfig
import kr.ohlab.android.recyclerviewgroup.support.ViewTypeResolver
import kotlin.reflect.KClass


class TRAdapter(
    internal val totalSpanCount: Int,
    internal var viewTypeResolver: ViewTypeResolver,
    internal var viewTypeConfigsMap: Map<Int, ViewTypeConfig>
) : RecyclerView.Adapter<TRViewHolder<*>>() {
    private var mDataList: MutableList<ItemBase<*>> = mutableListOf()

    fun getItem(position: Int) = mDataList[position]

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
    ): TRAdapter {
        @Suppress("UNCHECKED_CAST")
        callbacksOnBindBefore[clazz] = callback as (ItemBase<*>) -> Unit
        return this
    }

    fun <BINDING : ViewBinding, ITEM : ItemBase<BINDING>> registerOnBindAfter(
        clazz: KClass<out ITEM>,
        callback: (ITEM) -> Unit
    ): TRAdapter {
        @Suppress("UNCHECKED_CAST")
        callbacksOnBindAfter[clazz] = callback as (ItemBase<*>) -> Unit
        return this
    }


    fun <ITEM : ItemBase<*>> registerOnRecycledBefore(
        clazz: KClass<out ITEM>,
        callback: (ITEM) -> Unit
    ): TRAdapter {
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

    override fun getItemCount(): Int = mDataList.count()


    fun getItemOrNull(position: Int): ItemBase<*>? {
        return if (position >= 0 && position < this.itemCount) {
            getItem(position)
        } else {
            null
        }

    }

    fun lastOrNull() = if (this.itemCount > 0) this.getItem(this.itemCount - 1) else null
    fun firstOrNull() = if (this.itemCount > 0) this.getItem(0) else null


    fun replaceRawDataList(dataList: MutableList<ItemBase<*>>, refresh: Boolean = true) {
        mDataList = dataList
        if (refresh) {
            this.notifyDataSetChanged()
        }
    }


    fun replaceDataList(dataList: Collection<ItemBase<*>>, refresh: Boolean = true) {
        mDataList = dataList.toMutableList()
        if (refresh) {
            this.notifyDataSetChanged()
        }
    }

    fun replaceDataList(dataArray: Array<ItemBase<*>>, refresh: Boolean = true) {
        mDataList = dataArray.toMutableList()
        if (refresh) {
            this.notifyDataSetChanged()
        }
    }


    fun appendDataList(dataList: Collection<ItemBase<*>>, refresh: Boolean = true) {
        val beforeCount = mDataList.size
        mDataList.addAll(dataList)
        if (refresh) {
            this.notifyItemRangeInserted(beforeCount, dataList.size)
        }
    }

    fun appendDataList(dataArray: Array<out ItemBase<*>>, refresh: Boolean = true) {
        val beforeCount = mDataList.size
        mDataList.addAll(dataArray)
        if (refresh) {
            this.notifyItemRangeInserted(beforeCount, dataArray.size)
        }
    }

    fun appendData(data: ItemBase<*>, refresh: Boolean = true) {
        mDataList.add(data)
        if (refresh) {
            this.notifyItemInserted(mDataList.size - 1)
        }
    }


    fun prependDataList(dataList: Collection<ItemBase<*>>, refresh: Boolean = true) {
        mDataList.addAll(0, dataList)
        if (refresh) {
            notifyItemRangeInserted(0, dataList.size)
        }
    }

    fun prependDataList(dataArray: Array<out ItemBase<*>>, refresh: Boolean = true) {
        mDataList.addAll(0, dataArray.asList())
        if (refresh) {
            notifyItemRangeInserted(0, dataArray.size)
        }
    }

    fun prependData(data: ItemBase<*>, refresh: Boolean = true) {
        mDataList.add(0, data)
        if (refresh) {
            this.notifyItemInserted(0)
        }
    }

    fun replaceDataAt(position: Int, newData: ItemBase<*>, refresh: Boolean = true) {
        mDataList[position] = newData
        if (refresh) {
            this.notifyItemChanged(position)
        }
    }


    fun removeDataAt(position: Int, refresh: Boolean = true) {
        if (position < itemCount) {
            mDataList.removeAt(position)
            if (refresh) {
                this.notifyItemRemoved(position)
                // this.notifyItemRangeChanged(position, itemCount )
                // this.notifyDataSetChanged()
            }
        }
    }


    fun dataList(copied: Boolean): List<ItemBase<*>> {
        return if (copied) {
            mDataList.toMutableList()
        } else {
            mDataList
        }
    }

}
