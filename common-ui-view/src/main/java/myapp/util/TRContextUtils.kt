package myapp.util

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager


object TRContextUtils {
    fun fragmentManager(obj: Any): FragmentManager? = when (obj) {
        is FragmentActivity -> obj.supportFragmentManager
        is Fragment -> obj.childFragmentManager
        else -> null
    }

    fun activity(obj: Any): Activity? = when (obj) {
        is Activity -> obj
        is Fragment -> obj.activity
        else -> null
    }


    fun fragmentActivity(obj: Any): FragmentActivity? = when (obj) {
        is FragmentActivity -> obj
        is Fragment -> obj.activity
        else -> null
    }

    fun fragment(obj: Any): Fragment? = when (obj) {
        is Fragment -> obj
        else -> null
    }

    fun context(obj: Any): Context? = when (obj) {
        is Activity -> obj
        is Fragment -> obj.context
        is Context -> obj
        else -> null
    }
}
