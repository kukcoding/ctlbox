package myapp.ui.home.leftmenu.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.ohlab.android.recyclerviewgroup.ItemBase
import kr.ohlab.android.recyclerviewgroup.TRViewHolder
import myapp.ui.home.databinding.ViewholderLeftMenuNaviBinding
import myapp.util.Action2

class LeftMenuNaviItem(
) : ItemBase<ViewholderLeftMenuNaviBinding>() {

    companion object {
        @JvmStatic
        fun create(li: LayoutInflater, parentView: ViewGroup) =
            ViewholderLeftMenuNaviBinding.inflate(li, parentView, false)
    }

    var onWifiClickClick: Action2<View, LeftMenuNaviItem>? = null

    override fun onBindBefore(holder: TRViewHolder<ViewholderLeftMenuNaviBinding>) {
        holder.registerClickListener(holder.binding.layoutWifiBtn, onWifiClickClick)
        // holder.registerClickListener(holder.binding.imgviewAddBtn, onAddCameraClick)
        // holder.registerClickListener(holder.binding.txtviewMarketReview, onMarketReviewClick)
        // holder.registerClickListener(holder.binding.txtviewAppShare, onAppShareClick)
    }

    override fun onBind(
        holder: TRViewHolder<ViewholderLeftMenuNaviBinding>,
        position: Int
    ) {
        val b = holder.binding
    }
}
