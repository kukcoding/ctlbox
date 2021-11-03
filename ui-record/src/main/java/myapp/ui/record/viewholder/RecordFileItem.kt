package myapp.ui.record.viewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kr.ohlab.android.recyclerviewgroup.ItemBase
import kr.ohlab.android.recyclerviewgroup.TRViewHolder
import myapp.Cam
import myapp.data.entities.KuRecordFile
import myapp.extensions.formatDurationMilliInMinute
import myapp.extensions.humanReadableCount
import myapp.ui.record.databinding.ViewholderRecordFileBinding
import myapp.util.Action2
import myapp.util.Func1
import myapp.util.Func2
import org.threeten.bp.ZoneOffset

class RecordFileItem(
    val cameraIp: String,
    val recordFile: KuRecordFile,
    id: Long? = null
) : ItemBase<ViewholderRecordFileBinding>(id) {

    companion object {
        @JvmStatic
        fun create(li: LayoutInflater, parentView: ViewGroup) =
            ViewholderRecordFileBinding.inflate(li, parentView, false)
    }

    var onDownloadClick: Action2<View, RecordFileItem>? = null
    var onHolderClick: Action2<View, RecordFileItem>? = null
    var onHolderLongClick: Func2<View, RecordFileItem, Boolean>? = null
    var callbackIsEditing: Func1<RecordFileItem, Boolean>? = null

    override fun onBindBefore(holder: TRViewHolder<ViewholderRecordFileBinding>) {
        holder.registerClickListener(holder.binding.imgviewDownloadBtn, onDownloadClick)
        holder.registerClickListener(onHolderClick)
        holder.registerLongClickListener(onHolderLongClick)
    }

    private fun pad(num: Int): String {
        return if (num < 10) "0$num"
        else num.toString()
    }

    override fun onBind(
        holder: TRViewHolder<ViewholderRecordFileBinding>,
        position: Int
    ) {
        val b = holder.binding
        val from = this.recordFile.dateTime
        val to = from.plusNanos(recordFile.durationMilli * 1000 * 1000)
        b.txtviewYear.text = "${from.year}년 ${from.monthValue}월 ${from.dayOfMonth}일"
//        b.txtviewTitle.text =
//            "${from.monthValue}월 ${from.dayOfMonth}일 ${from.hour}:${pad(from.minute)} ~ ${to.hour}:${
//                pad(
//                    to.minute
//                )
//            }"

        b.txtviewTitle.text = "${from.hour}시 ${pad(from.minute)}분 ${pad(from.second)}초"

        b.txtviewDuration.text = formatDurationMilliInMinute(recordFile.durationMilli)
        b.txtviewFileSize.text = humanReadableCount(recordFile.fileSize)
        b.txtviewResolution.text = "${recordFile.width}x${recordFile.height}"
        b.txtviewFps.text = "${recordFile.fps} fps"

        Glide.with(b.imgviewThumbnail)
            .load(
                Cam.thumbnailUrl(
                    ip = cameraIp,
                    fileId = recordFile.fileId,
                    timestamp = recordFile.dateTime.toEpochSecond(ZoneOffset.UTC)
                )
            )
            .into(b.imgviewThumbnail)

        this.updateSelection(isHolderSelected)
    }

    fun updateSelection(selected: Boolean) {
        val b = this.viewHolder?.binding ?: return
        val editing = callbackIsEditing?.invoke(this) ?: false


        if (editing) {
            // 편집 모드
            b.imgviewCheck.isVisible = true // 선택된 경우 체크버튼 표시
            b.imgviewCheck.isSelected = selected
            b.imgviewDownloadBtn.isVisible = false  // 편집모드에서 다운로드 버튼 숨김
            b.overlaySelection.isVisible = selected
        } else {
            b.imgviewCheck.isVisible = false
            b.imgviewDownloadBtn.isVisible = true
            b.overlaySelection.isVisible = false
        }
    }
}
