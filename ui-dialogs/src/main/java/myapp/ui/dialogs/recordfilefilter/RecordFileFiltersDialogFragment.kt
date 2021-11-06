package myapp.ui.dialogs.recordfilefilter


import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import kr.ohlab.android.recyclerviewgroup.DefaultHolderTracker
import kr.ohlab.android.recyclerviewgroup.TRListAdapter
import kr.ohlab.android.recyclerviewgroup.support.buildListAdapter
import myapp.data.entities.RecordFileFilter
import myapp.ui.dialogs.databinding.DialogRecordFileFiltersBinding
import myapp.ui.dialogs.recordfilefilter.viewholder.RecordFileFilterItem
import myapp.util.Action1
import myapp.util.AndroidUtils
import myapp.util.AndroidUtils.dpf
import kotlin.math.min

@AndroidEntryPoint
class RecordFileFiltersDialogFragment : DialogFragment() {
    companion object {
        fun newInstance() = RecordFileFiltersDialogFragment()
    }

    // argument
    var mArgRecordFileFilters: List<RecordFileFilter> = emptyList()

    var onDismissListener: Action1<RecordFileFilter?>? = null
    private var mResultFilterKey: RecordFileFilter? = null

    private lateinit var mAdapter: TRListAdapter
    private val mViewModel: RecordFileFiltersDialogViewModel by viewModels()
    private lateinit var mBind: DialogRecordFileFiltersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onDismissListener == null) {
            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBind = DialogRecordFileFiltersBinding.inflate(inflater, container, false)
        mBind.vm = mViewModel
        mBind.lifecycleOwner = this
        return mBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customInit()
        setupEvents()
    }

    private fun customInit() {
        setupRecyclerView()
    }

    private fun setupEvents() {
        mViewModel.replaceFileFilters(mArgRecordFileFilters)

        mViewModel.viewItems.asLiveData().observe(viewLifecycleOwner, {
            mAdapter.submitList(it)
        })

        mViewModel.liveFieldOf(RecordFileFiltersViewState::selectedFilter).observe(viewLifecycleOwner, {
            mAdapter.notifyDataSetChanged()
        })

//        // 완료버튼 클릭
//        mBind.btDone.setOnClickListener {
//            dismissWithDataChanged(mViewModel.currentState().selectedFilter)
//        }

        // 닫기 버튼 클릭
        mBind.layoutCloseBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRecyclerView() {

        mAdapter = buildListAdapter(mBind.recyclerView, 1) {
            addViewType(RecordFileFilterItem::class)
        }

        mAdapter.apply {
            registerOnBindBefore(RecordFileFilterItem::class) { item ->
                item.onHolderClick = ::onHolderClickRecordFileFilter
            }
        }

        mAdapter.holderTracker = object : DefaultHolderTracker() {
            override fun holderSelected(item: Any): Boolean {
                val fileId = (item as? RecordFileFilterItem)?.filter?.key ?: false
                val state = mViewModel.currentState()
                return state.selectedFilter?.key == fileId
            }
        }
    }


    // 뷰홀더 - RecordFileFilter 클릭
    private fun onHolderClickRecordFileFilter(view: View, item: RecordFileFilterItem) {
        // mViewModel.submitAction(RecordFileFiltersAction.SelectFilter(filter = item.filter))
        mResultFilterKey = item.filter
        dismiss()
    }


    override fun onStart() {
        super.onStart()
        val ctx = this.context ?: return
        val window = dialog?.window ?: return
        val w = preferWindowWidth(ctx)
        val h = AndroidUtils.screenHeight(ctx) * 0.7
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(w.toInt(), h.toInt())
    }

    private fun preferWindowWidth(ctx: Context): Float {
        val preferWidth = dpf(320)
        val screenWidth = AndroidUtils.screenSmallSide(ctx)


        return if (preferWidth < 0) preferWidth else min(preferWidth, screenWidth * 0.85f)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(mResultFilterKey)
    }
}

