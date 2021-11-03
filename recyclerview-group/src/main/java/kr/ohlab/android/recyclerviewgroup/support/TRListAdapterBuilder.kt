package kr.ohlab.android.recyclerviewgroup.support

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.ohlab.android.recyclerviewgroup.TRListAdapter
import kr.ohlab.android.recyclerviewgroup.ViewTypeMatcher
import kotlin.concurrent.thread
import kotlin.math.min


typealias AdapterCreator = (spanCount: Int, viewTypeResolver: ViewTypeResolver, viewTypeConfigsMap: Map<Int, ViewTypeConfig>) -> TRListAdapter


open class TRListAdapterBuilder(val spanCount: Int, val hasStableId: Boolean = false) {

    private var viewTypeMatchers = mutableListOf<ViewTypeMatcher>()
    private var viewTypeResolver: ViewTypeResolver? = null
    private var viewTypeConfigsMap: Map<Int, ViewTypeConfig>? = null

    fun configure(block: AdapterViewTypeBuilder.() -> Unit): TRListAdapterBuilder {
        val config = AdapterViewTypeBuilder(spanCount = spanCount)
        config.block()
        this.viewTypeMatchers = config.viewTypeMatchers
        this.viewTypeConfigsMap = config.viewTypeConfigs.map { it.viewType to it }.toMap()
        this.viewTypeResolver = object : ViewTypeResolver {
            override fun resolve(position: Int, item: Any): Int {
                for (i in viewTypeMatchers.indices) {
                    val viewType = viewTypeMatchers[i].invoke(position, item)
                    if (viewType != null) return viewType
                }

                // Log.w("TRListAdapterConfigurer", "cannot find for data '${item::class}',  ${item}")
                throw java.lang.IllegalStateException("cannot find for data '${item::class}',  ${item}")
            }
        }
        return this
    }


    protected open fun setupLayoutManager(recyclerView: RecyclerView, adapter: TRListAdapter) {
        if (spanCount == 1) {
            val layoutMgr = LinearLayoutManager(recyclerView.context)
            recyclerView.layoutManager = layoutMgr
        } else {
            val layoutMgr = GridLayoutManager(recyclerView.context, spanCount)
            layoutMgr.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val cnt = adapter.getSpanSizeAt(position)
                    return if (cnt > 0) min(cnt, spanCount) else spanCount
                }
            }
            layoutMgr.spanSizeLookup.isSpanIndexCacheEnabled = true
            recyclerView.layoutManager = layoutMgr
        }
    }

    fun <T : TRListAdapter> createOrRebuildAdapter(recyclerView: RecyclerView, adapter: T?): T {
        return if (adapter != null && adapter.totalSpanCount == spanCount) {
            rebuildAdapter(recyclerView = recyclerView, adapter = adapter)
        } else {
            createAdapter(recyclerView = recyclerView) as T
        }
    }

    private fun <T : TRListAdapter> rebuildAdapter(recyclerView: RecyclerView, adapter: T): T {
        adapter.viewTypeResolver = this.viewTypeResolver!!
        adapter.viewTypeConfigsMap = this.viewTypeConfigsMap!!
        setupLayoutManager(recyclerView, adapter)
        recyclerView.adapter = adapter
        return adapter
    }

    fun newAdapter(adapterCreator: AdapterCreator): TRListAdapterBuilder {
        this.createNewAdapter = adapterCreator
        return this
    }

    private var createNewAdapter: AdapterCreator? = null

    internal fun createAdapter(recyclerView: RecyclerView): TRListAdapter {
        val adapter = createNewAdapter?.invoke(this.spanCount, this.viewTypeResolver!!, this.viewTypeConfigsMap!!)
            ?: TRListAdapter(
                totalSpanCount = spanCount,
                viewTypeResolver = this.viewTypeResolver!!,
                viewTypeConfigsMap = this.viewTypeConfigsMap!!
            )

        adapter.setHasStableIds(hasStableId)
        thread {
            adapter.bindingItemCreator.prepareViewTypes(adapter.viewTypeConfigsMap.keys)
        }

        setupLayoutManager(recyclerView, adapter)
        recyclerView.adapter = adapter
        return adapter
    }
}


class TRHorizontalListAdapterBuilder(
    spanCount: Int, hasStableId: Boolean = false
) : TRListAdapterBuilder(spanCount = spanCount, hasStableId = hasStableId) {

    open override fun setupLayoutManager(recyclerView: RecyclerView, adapter: TRListAdapter) {
        if (spanCount == 1) {
            val layoutMgr = LinearLayoutManager(recyclerView.context, RecyclerView.HORIZONTAL, false)
            recyclerView.layoutManager = layoutMgr
        } else {
            val layoutMgr = GridLayoutManager(recyclerView.context, spanCount, RecyclerView.HORIZONTAL, false)
            layoutMgr.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val cnt = adapter.getSpanSizeAt(position)
                    return if (cnt > 0) min(cnt, spanCount) else spanCount
                }
            }
            layoutMgr.spanSizeLookup.isSpanIndexCacheEnabled = true
            recyclerView.layoutManager = layoutMgr
        }
    }
}
