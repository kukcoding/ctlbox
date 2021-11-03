package kr.ohlab.android.recyclerviewgroup.support


interface ViewTypeResolver {
    fun resolve(position: Int, item: Any): Int
}
