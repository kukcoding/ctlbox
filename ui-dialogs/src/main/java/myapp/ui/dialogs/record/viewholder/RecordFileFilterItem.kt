package myapp.ui.dialogs.record.viewholder


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import kr.ohlab.android.recyclerviewgroup.ItemBase
import kr.ohlab.android.recyclerviewgroup.TRViewHolder
import myapp.data.entities.RecordFileFilter
import myapp.ui.dialogs.databinding.ViewholderRecordFileFilterBinding
import myapp.util.Action2


private fun pad(v: Int) = if (v >= 10) v.toString() else "0${v}"

class RecordFileFilterItem(
    val filter: RecordFileFilter,
    id: Long? = null
) : ItemBase<ViewholderRecordFileFilterBinding>(id) {

    companion object {
        @JvmStatic
        fun create(li: LayoutInflater, parentView: ViewGroup) =
            ViewholderRecordFileFilterBinding.inflate(li, parentView, false)
    }


    var onHolderClick: Action2<View, RecordFileFilterItem>? = null

    override fun onBindBefore(holder: TRViewHolder<ViewholderRecordFileFilterBinding>) {
        holder.registerClickListener(onHolderClick)
    }


    fun viewUpdate() {
        val binding = viewHolder?.binding ?: return
        update(binding)
    }

    override fun onBind(
        holder: TRViewHolder<ViewholderRecordFileFilterBinding>,
        position: Int
    ) {
        val b = holder.binding
        update(b)
    }

    private fun update(
        b: ViewholderRecordFileFilterBinding
    ) {
        b.txtviewDate.text = "${filter.year}.${pad(filter.monthValue)}.${pad(filter.dayOfMonth)}"
        b.txtviewHour.text = "${filter.hour}시"
        b.txtviewFileCount.text = buildSpannedString {
            color(Color.WHITE) {
                bold {
                    append(filter.count.toString())
                }
            }
            scale(0.7f) {
                append(" Files")
            }
        }
        b.imgviewRadio.isSelected = isHolderSelected
    }
}
