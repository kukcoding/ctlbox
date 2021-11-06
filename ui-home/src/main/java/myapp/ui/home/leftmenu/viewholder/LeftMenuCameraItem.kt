package myapp.ui.home.leftmenu.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import kr.ohlab.android.recyclerviewgroup.ItemBase
import kr.ohlab.android.recyclerviewgroup.TRViewHolder
import myapp.data.entities.KuCamera
import myapp.ui.home.databinding.ViewholderLeftMenuCameraBinding
import myapp.util.Action2

class LeftMenuCameraItem(
    val camera: KuCamera
) : ItemBase<ViewholderLeftMenuCameraBinding>() {

    companion object {
        @JvmStatic
        fun create(li: LayoutInflater, parentView: ViewGroup) =
            ViewholderLeftMenuCameraBinding.inflate(li, parentView, false)
    }

    var onHolderClick: Action2<View, LeftMenuCameraItem>? = null
    var onDeleteClick: Action2<View, LeftMenuCameraItem>? = null
    var onDisconnectClick: Action2<View, LeftMenuCameraItem>? = null


    override fun onBindBefore(holder: TRViewHolder<ViewholderLeftMenuCameraBinding>) {
        holder.registerClickListener(holder.binding.layoutContent, onHolderClick)
        holder.registerClickListener(holder.binding.imgviewDeleteBtn, onDeleteClick)
        holder.registerClickListener(holder.binding.btDisconnect, onDisconnectClick)
    }

    override fun onUnbind(holder: TRViewHolder<ViewholderLeftMenuCameraBinding>) {
        onHolderClick = null
    }

    override fun onBind(
        holder: TRViewHolder<ViewholderLeftMenuCameraBinding>,
        position: Int
    ) {
        val binding = holder.binding
        binding.txtviewTitle.text = camera.cameraName
        binding.txtviewCameraId.text = camera.cameraId
        val selected = isHolderSelected
        if (selected) {
            binding.btDisconnect.isVisible = true
            binding.imgviewDeleteBtn.isVisible = false
            binding.imgviewCamera.isSelected = true
        } else {
            binding.btDisconnect.isVisible = false
            binding.imgviewDeleteBtn.isVisible = true
            binding.imgviewCamera.isSelected = false
        }
    }
}
