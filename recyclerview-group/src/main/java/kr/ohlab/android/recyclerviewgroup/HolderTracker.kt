package kr.ohlab.android.recyclerviewgroup

interface HolderTracker {
    fun holderSelected(item: Any): Boolean
    fun holderSelectable(item: Any): Boolean
    fun holderEditing(item: Any): Boolean
    fun holderChecked(item: Any): Boolean
}
