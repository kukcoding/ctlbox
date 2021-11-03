package kr.ohlab.android.recyclerviewgroup

open class DefaultHolderTracker : HolderTracker {
    override fun holderSelected(item: Any): Boolean = false
    override fun holderSelectable(item: Any): Boolean = false
    override fun holderEditing(item: Any): Boolean = false
    override fun holderChecked(item: Any): Boolean = false
}

