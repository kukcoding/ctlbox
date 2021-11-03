package kr.ohlab.android.recyclerviewgroup

import androidx.recyclerview.widget.DiffUtil


internal class DiffItemCallback<ITEM : ItemBase<*>> : DiffUtil.ItemCallback<ITEM>() {
    override fun areItemsTheSame(oldItem: ITEM, newItem: ITEM): Boolean {
        return oldItem.isSameAs(newItem)
    }

    override fun areContentsTheSame(oldItem: ITEM, newItem: ITEM): Boolean {
        return oldItem.hasSameContentAs(newItem)
    }
}
