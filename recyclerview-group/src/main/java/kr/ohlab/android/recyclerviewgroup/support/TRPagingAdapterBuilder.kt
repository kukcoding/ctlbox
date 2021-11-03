package kr.ohlab.android.recyclerviewgroup.support

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.ohlab.android.recyclerviewgroup.TRPagingAdapter
import kr.ohlab.android.recyclerviewgroup.ViewTypeMatcher
import kotlin.concurrent.thread
import kotlin.math.min


class TRPagingAdapterBuilder(val spanCount: Int) {
    private var viewTypeMatchers = mutableListOf<ViewTypeMatcher>()
    private var viewTypeResolver: ViewTypeResolver? = null
    private var viewTypeConfigsMap: Map<Int, ViewTypeConfig>? = null

    fun configure(block: AdapterViewTypeBuilder.() -> Unit): TRPagingAdapterBuilder {
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

    private fun setupLayoutManager(recyclerView: RecyclerView, adapter: TRPagingAdapter) {
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

    fun createOrRebuildAdapter(recyclerView: RecyclerView, adapter: TRPagingAdapter?): TRPagingAdapter {
        return if (adapter == null) {
            createAdapter(recyclerView = recyclerView)
        } else {
            rebuildAdapter(recyclerView = recyclerView, adapter = adapter)
        }
    }

    fun rebuildAdapter(recyclerView: RecyclerView, adapter: TRPagingAdapter): TRPagingAdapter {
        adapter.viewTypeResolver = this.viewTypeResolver!!
        adapter.viewTypeConfigsMap = this.viewTypeConfigsMap!!
        setupLayoutManager(recyclerView, adapter)
        recyclerView.adapter = adapter
        return adapter
    }

    fun createAdapter(recyclerView: RecyclerView): TRPagingAdapter {
        val adapter = TRPagingAdapter(
            totalSpanCount = this.spanCount,
            viewTypeResolver = this.viewTypeResolver!!,
            viewTypeConfigsMap = this.viewTypeConfigsMap!!
        )
        thread {
            adapter.bindingItemCreator.prepareViewTypes(adapter.viewTypeConfigsMap.keys)
        }

        setupLayoutManager(recyclerView, adapter)
        recyclerView.adapter = adapter
        return adapter
    }
}


fun buildPagingAdapter(
    recyclerView: RecyclerView,
    spanCount: Int,
    block: AdapterViewTypeBuilder.() -> Unit
): TRPagingAdapter {
    val builder = TRPagingAdapterBuilder(spanCount = spanCount)
    builder.configure(block)
    return builder.createAdapter(recyclerView)
}
