package kr.ohlab.android.recyclerviewgroup.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.ohlab.android.recyclerviewgroup.ItemBase
import kr.ohlab.android.recyclerviewgroup.TRAdapter
import kr.ohlab.android.recyclerviewgroup.TRListAdapter


fun LinearLayoutManager.findCompleteVisibleItemPositionRange(expandSize: Int = 0): IntRange? {
    val mgr = this
    if (mgr.itemCount < 0) return null

    val begin = mgr.findFirstCompletelyVisibleItemPosition()
    if (begin < 0) return null
    val end = mgr.findLastCompletelyVisibleItemPosition()
    if (end < 0) return null
    return if (expandSize != 0) {
        // val a1 = maxOf(begin - expandSize, 0)
        //val a2 = minOf(end + expandSize, mgr.itemCount - 1)
        val a1 = (begin - expandSize).coerceIn(0, mgr.itemCount - 1)
        val a2 = (end + expandSize).coerceIn(a1, mgr.itemCount - 1)
        return a1..a2
    } else {
        begin..end
    }
}

fun LinearLayoutManager.findVisibleItemPositionRange(expandSize: Int = 0): IntRange? {
    val mgr = this
    if (mgr.itemCount < 0) return null

    val begin = mgr.findFirstVisibleItemPosition()
    if (begin < 0) return null
    val end = mgr.findLastVisibleItemPosition()
    if (end < 0) return null
    return if (expandSize > 0) {
        // val a1 = maxOf(begin - expandSize, 0)
        // val a2 = minOf(end + expandSize, mgr.itemCount - 1)
        val a1 = (begin - expandSize).coerceIn(0, mgr.itemCount - 1)
        val a2 = (end + expandSize).coerceIn(a1, mgr.itemCount - 1)
        return a1..a2
    } else {
        begin..end
    }
}

fun RecyclerView.findVisibleViewHolders(
    expandSize: Int = 0,
    predicate: (RecyclerView.ViewHolder) -> Boolean
): Sequence<RecyclerView.ViewHolder> {
    // check support Staggered Layout
    val mgr = layoutManager as? LinearLayoutManager ?: return emptySequence()
    if (mgr.itemCount <= 0) return emptySequence()
    val range = mgr.findVisibleItemPositionRange(expandSize = expandSize) ?: return emptySequence()
    return range.asSequence().mapNotNull { this.findViewHolderForAdapterPosition(it) }
        .filter { predicate(it) }
}


inline fun <reified T : ItemBase<*>> TRListAdapter.forEachItems(block: (Int, T) -> Unit) {
    this.currentList.forEachIndexed { idx, item ->
        if (item is T) {
            block(idx, item)
        }
    }
}


inline fun <reified T : ItemBase<*>, R : Any> TRListAdapter.mapItems(crossinline block: (Int, T) -> R?): Sequence<R?> {
    return this.currentList.asSequence().mapIndexed { idx, item ->
        if (item is T) block(idx, item) else null
    }
}

/**
 * ex) mAdapter.mapItemsNotNull { idx, item: BibleReadmarkItem -> ... }
 */
inline fun <reified T : ItemBase<*>, R : Any> TRListAdapter.mapItemsNotNull(crossinline block: (Int, T) -> R?): Sequence<R> {
    return this.currentList.asSequence().mapIndexedNotNull { idx, item ->
        if (item is T) block(idx, item) else null
    }
}


inline fun <reified T : ItemBase<*>> TRAdapter.forEachItems(copied: Boolean, block: (Int, T) -> Unit) {
    this.dataList(copied = copied).forEachIndexed { idx, item ->
        if (item is T) {
            block(idx, item)
        }
    }
}

inline fun <reified T : ItemBase<*>, R : Any> TRAdapter.mapItems(crossinline block: (Int, T) -> R?): Sequence<R?> {
    return this.dataList(copied = false).asSequence().mapIndexed { idx, item ->
        if (item is T) block(idx, item) else null
    }
}

inline fun <reified T : ItemBase<*>, R : Any> TRAdapter.mapItemsNotNull(crossinline block: (Int, T) -> R?): Sequence<R> {
    return this.dataList(copied = false).asSequence().mapIndexedNotNull { idx, item ->
        if (item is T) block(idx, item) else null
    }
}

