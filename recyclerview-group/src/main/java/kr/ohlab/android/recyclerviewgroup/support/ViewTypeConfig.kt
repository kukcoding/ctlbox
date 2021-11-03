package kr.ohlab.android.recyclerviewgroup.support

import kotlin.reflect.KClass

data class ViewTypeConfig(
    val viewType: Int,
    val spanSize: Int,
    val binderClass: KClass<*>,
    var layoutResId: Int? = null
)
