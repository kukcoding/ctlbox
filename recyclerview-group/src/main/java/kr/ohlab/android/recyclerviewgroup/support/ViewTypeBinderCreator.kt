package kr.ohlab.android.recyclerviewgroup.support


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import timber.log.Timber
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis


interface ViewTypeBinderCreator {
    fun create(parentView: ViewGroup, viewType: Int): ViewBinding
    fun prepareViewTypes(viewTypes: Collection<Int>)
}


class SimpleViewTypeBinderCreator(
    private var binderClassProvider: (viewType: Int) -> KClass<*>
) : ViewTypeBinderCreator {
    private val methodCache = mutableMapOf<KClass<*>, Method>()

    override fun prepareViewTypes(viewTypes: Collection<Int>) {
        val duration = measureTimeMillis {
            viewTypes.forEach { findCreatorMethod(it) }
        }
        Timber.d("prepareViewTypes finished ${duration}ms, $viewTypes")
    }

    private fun findCreatorMethod(viewType: Int): Method {
        val clz = binderClassProvider(viewType)
        val m = methodCache[clz] ?: clz.java.getMethod(
            "create",
            LayoutInflater::class.java,
            ViewGroup::class.java
        )
        methodCache[clz] = m
        return m
    }

    override fun create(parentView: ViewGroup, viewType: Int): ViewBinding {
        val m = findCreatorMethod(viewType = viewType)
        val li = LayoutInflater.from(parentView.context)
        return m.invoke(null, li, parentView)!! as ViewBinding
    }
}
