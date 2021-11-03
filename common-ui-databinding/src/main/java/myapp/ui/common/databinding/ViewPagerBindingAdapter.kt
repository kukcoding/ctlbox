package myapp.ui.common.databinding

import androidx.annotation.IdRes
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

@Suppress("unused")
object ViewPagerBindingAdapter {
    @JvmStatic
    @BindingAdapter("tabPosition")
    fun setTabPosition(viewPager: ViewPager2, tabPosition: Int) {
        viewPager.currentItem = tabPosition
    }

    @JvmStatic
    @BindingAdapter("tabPositionAttrChanged")
    fun tabPositionInverseBindingListener(
        viewPager: ViewPager2,
        listener: InverseBindingListener?
    ) {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                listener?.onChange()
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "tabPosition", event = "tabPositionAttrChanged")
    @IdRes
    fun getTabPosition(view: ViewPager2): Int {
        return view.currentItem
    }


    @JvmStatic
    @BindingAdapter("tabPosition")
    fun setTabPosition(viewPager: ViewPager, tabPosition: Int) {
        viewPager.currentItem = tabPosition
    }

    @JvmStatic
    @BindingAdapter("tabPositionAttrChanged")
    fun tabPositionInverseBindingListener(
        viewPager: ViewPager,
        listener: InverseBindingListener?
    ) {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                listener?.onChange()
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
    }

    @JvmStatic
    @InverseBindingAdapter(attribute = "tabPosition", event = "tabPositionAttrChanged")
    @IdRes
    fun getTabPosition(view: ViewPager): Int {
        return view.currentItem
    }

//
//    @JvmStatic
//    @BindingAdapter("tabIndex")
//    fun setTabIndex(viewPager: ViewPager, tabIndex: Int) {
//        viewPager.currentItem = tabIndex
//    }
//
//    @JvmStatic
//    @BindingAdapter("tabIndexAttrChanged")
//    fun tabIndexInverseBindingListener(viewPager: ViewPager, listener: InverseBindingListener?) {
//        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//            }
//
//            override fun onPageSelected(position: Int) {
//                listener?.onChange()
//            }
//
//            override fun onPageScrollStateChanged(state: Int) {
//            }
//        })
//    }
//
//    @JvmStatic
//    @InverseBindingAdapter(attribute = "tabIndex", event = "tabIndexAttrChanged")
//    @IdRes
//    fun getTabIndex(view: ViewPager): Int {
//        return view.currentItem
//    }
}
