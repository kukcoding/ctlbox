package myapp.ui.home.leftmenu.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kr.ohlab.android.recyclerviewgroup.ItemBase
import kr.ohlab.android.recyclerviewgroup.TRViewHolder
import myapp.ui.home.databinding.ViewholderLeftMenuBinding
import myapp.ui.home.leftmenu.LeftMenu
import myapp.util.Action2

class LeftMenuItem(
    val menu: LeftMenu,
    private val showDivider: Boolean = false
) : ItemBase<ViewholderLeftMenuBinding>() {

    companion object {
        @JvmStatic
        fun create(li: LayoutInflater, parentView: ViewGroup) =
            ViewholderLeftMenuBinding.inflate(li, parentView, false)
    }

    var onHolderClick: Action2<View, LeftMenuItem>? = null

    override fun onBindBefore(holder: TRViewHolder<ViewholderLeftMenuBinding>) {
        holder.registerClickListener(onHolderClick)
    }

    override fun onUnbind(holder: TRViewHolder<ViewholderLeftMenuBinding>) {
        onHolderClick = null
    }

    override fun onBind(
        holder: TRViewHolder<ViewholderLeftMenuBinding>,
        position: Int
    ) {
        val binding = holder.binding
        binding.txtviewTitle.setText(menu.labelResId)
        binding.imgviewMenuIcon.setImageResource(menu.iconResId)
        binding.divider.isVisible = showDivider
    }
}
