package kr.ohlab.android.recyclerviewgroup.support

import androidx.recyclerview.widget.RecyclerView
import kr.ohlab.android.recyclerviewgroup.ItemBase
import kr.ohlab.android.recyclerviewgroup.TRListAdapter
import kr.ohlab.android.recyclerviewgroup.ViewTypeMatcher
import kotlin.reflect.KClass

class AdapterViewTypeBuilder(
    private val spanCount: Int
) {
    val viewTypeMatchers = mutableListOf<ViewTypeMatcher>()
    val viewTypeConfigs = mutableListOf<ViewTypeConfig>()

    fun <T : ItemBase<*>> addViewType(
        spanSize: Int,
        viewType: Int,
        itemClass: KClass<T>,
        matcher: ViewTypeMatcher
    ): AdapterViewTypeBuilder {
        viewTypeConfigs.add(
            ViewTypeConfig(
                viewType = viewType,
                spanSize = spanSize.coerceIn(1, spanCount),
                binderClass = itemClass
            )
        )
        viewTypeMatchers.add(matcher)
        return this
    }

    fun <T : ItemBase<*>> addViewType(
        itemClass: KClass<T>
    ): AdapterViewTypeBuilder {
        return addViewType(spanSize = 9999, itemClass = itemClass)
    }


    fun <T : ItemBase<*>> addViewType(
        spanSize: Int,
        itemClass: KClass<T>
    ): AdapterViewTypeBuilder {
        val viewType = viewTypeConfigs.size
        val matchBy: ViewTypeMatcher = { _, item ->
            if (itemClass.isInstance(item)) viewType else null
        }
        addViewType(
            viewType = viewType,
            spanSize = spanSize.coerceIn(1, spanCount),
            itemClass = itemClass,
            matcher = matchBy
        )
        return this
    }
}


fun buildListAdapter(
    recyclerView: RecyclerView,
    spanCount: Int,
    hasStableId: Boolean = false,
    block: AdapterViewTypeBuilder.() -> Unit
): TRListAdapter {
    val builder = TRListAdapterBuilder(spanCount = spanCount, hasStableId = hasStableId)
    builder.configure(block)
    return builder.createAdapter(recyclerView)
}

// TODO spanCount를 제거해야 하는거 아닌가?
fun buildHorizontalListAdapter(
    recyclerView: RecyclerView,
    spanCount: Int,
    hasStableId: Boolean = false,
    block: AdapterViewTypeBuilder.() -> Unit
): TRListAdapter {
    val builder = TRHorizontalListAdapterBuilder(spanCount = spanCount, hasStableId = hasStableId)
    builder.configure(block)
    return builder.createAdapter(recyclerView)
}
