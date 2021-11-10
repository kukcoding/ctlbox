package myapp.ui.record


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.ohlab.android.permission.TRPermissionResult
import kr.ohlab.android.recyclerviewgroup.DefaultHolderTracker
import kr.ohlab.android.recyclerviewgroup.TRListAdapter
import kr.ohlab.android.recyclerviewgroup.support.buildListAdapter
import myapp.Cam
import myapp.data.entities.KuRecordFile
import myapp.extensions.alert
import myapp.extensions.confirmOk
import myapp.extensions.resources.resColor
import myapp.permissions.PermissionConfig
import myapp.permissions.PermissionUtils
import myapp.ui.dialogs.CameraDialogs
import myapp.ui.player.FilePlayerActivity
import myapp.ui.record.databinding.FragmentRecordFilesBinding
import myapp.ui.record.viewholder.RecordFileItem
import splitties.snackbar.snack
import timber.log.Timber


@AndroidEntryPoint
class RecordFilesFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = RecordFilesFragment().apply {
        }
    }

    private val mViewModel: RecordFilesFragmentViewModel by viewModels()
    private lateinit var mBind: FragmentRecordFilesBinding

    private lateinit var mAdapter: TRListAdapter
    private var mSelectAll = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBind = FragmentRecordFilesBinding.inflate(inflater, container, false)
        mBind.lifecycleOwner = this
        mBind.vm = mViewModel
        return mBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customInit()
        setupEvents()

        // 백키핸들러 등록, call reverse order, (FIRST-IN-LAST-CALLED)
        listOf(
            filterCancelBackPressedCallback,
            editingCancelBackPressedCallback
        ).forEach {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it)
        }
    }

    private fun customInit() {
        setupRecyclerView()
    }

    private fun setupEvents() {
        mBind.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        // swipeRefreshLayout 새로 고침
        mBind.swipeRefreshLayout.setOnRefreshListener {
            mViewModel.submitAction(RecordFilesFragmentAction.Refresh)
        }

        // 새로고침 버튼 클릭 - empty view
        mBind.btRefresh.setOnClickListener {
            mViewModel.submitAction(RecordFilesFragmentAction.Refresh)
        }

        // 로딩이 끝나면 swipeRefreshLayout 도 끝나게
        mViewModel.isLoadingLive.observe(viewLifecycleOwner, { loading ->
            if (!loading) {
                mBind.swipeRefreshLayout.isRefreshing = false
            }
        })

        // 편집 버튼 클릭
        mBind.layoutEditBtn.setOnClickListener {
            mViewModel.submitAction(RecordFilesFragmentAction.ToggleEditing)
        }

        // 편집 취소 버튼 클릭
        mBind.layoutEditCancelBtn.setOnClickListener {
            mViewModel.submitAction(RecordFilesFragmentAction.ToggleEditing)
        }

        // filter 버튼 클릭
        mBind.layoutFilterBtn.setOnClickListener {
            val filter = mViewModel.currentState().filter
            if (filter != null) {
                mViewModel.submitAction(RecordFilesFragmentAction.SetFilter(null))
                return@setOnClickListener
            }
            lifecycleScope.launch {
                Timber.d(mViewModel.createRecordFileFilters().toString())

                CameraDialogs.openRecordFileFilters(
                    fm = childFragmentManager,
                    recordFileFilters = mViewModel.createRecordFileFilters()
                ) { filter ->
                    mViewModel.submitAction(RecordFilesFragmentAction.SetFilter(filter))
                }
                // mViewModel.submitAction(RecordFilesFragmentAction.ToggleEditing)
            }
        }
        mViewModel.isFilterOnLive.observe(viewLifecycleOwner, {filterOn ->
            if(filterOn) {
                mBind.layoutStatusBar.setBackgroundColor(resColor(R.color.black_transparent_10))
            } else {
                mBind.layoutStatusBar.setBackgroundColor(resColor(R.color.transparent))
            }
        })

        // 삭제 버튼 클릭
        mBind.layoutDeleteBtn.setOnClickListener {
            val fileIds = mViewModel.currentState().checkedFileIds
            if (fileIds.isEmpty()) {
                alert("선택된 항목이 없습니다")
                return@setOnClickListener
            }

            lifecycleScope.launch {
                mViewModel.removingState.addLoader()
                var successCnt = 0
                var failCnt = 0
                fileIds.forEachIndexed { _, fileId ->
                    if (isActive) {
                        try {
                            mViewModel.removeRecordFile(fileId = fileId)
                            mViewModel.submitAction(action = RecordFilesFragmentAction.RemoveViewItemForce(fileId = fileId))
                            successCnt++
                        } catch (err: Throwable) {
                            Timber.w(err)
                            failCnt++
                        }
                    }
                }
                if (successCnt > 0 && failCnt > 0) {
                    mBind.root.snack("삭제 중 ${successCnt}개 성공, ${failCnt}개 실패했습니다. 잠시 후 다시 시도해주세요")
                } else if (successCnt > 0) {
                    mBind.root.snack("${successCnt}개의 파일을 삭제했습니다")
                } else if (failCnt > 0) {
                    mBind.root.snack("삭제 동작이 실패했습니다")
                }
                mViewModel.removingState.removeLoader()
            }
        }

        // 전체 선택 버튼 클릭
        mBind.layoutCheckAllBtn.setOnClickListener {
            val newSelectAll = !mSelectAll
            if (newSelectAll) {
                mViewModel.submitAction(RecordFilesFragmentAction.SelectAll)
            } else {
                mViewModel.submitAction(RecordFilesFragmentAction.ClearAllSelection)
            }
            mSelectAll = newSelectAll
            mBind.imgviewCheck.isSelected = mSelectAll
        }

        // 편집모드 백키 등록
        mViewModel.liveFieldOf(RecordFilesFragmentState::isEditing).observe(viewLifecycleOwner, { editing ->
            // 백키 핸들러 활성화
            editingCancelBackPressedCallback.isEnabled = editing
            if (editing) {
                mBind.toolbar.navigationIcon = null
            } else {
                mBind.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            }
        })

        // 필터 백키 등록
        mViewModel.isFilterOnLive.observe(viewLifecycleOwner, { filterOn ->
            // 백키 핸들러 활성화
            filterCancelBackPressedCallback.isEnabled = filterOn
        })

        // 편집 모드 변경
        mViewModel.liveFieldOf(RecordFilesFragmentState::isEditing).observe(viewLifecycleOwner, {
            mAdapter.notifyDataSetChanged()
        })

        // 선택된 항목이 변경되면, 새로고침
        mViewModel.liveFieldOf(RecordFilesFragmentState::checkedFileIds).observe(viewLifecycleOwner, {
            mAdapter.notifyDataSetChanged()
        })

        // 리싸이클러뷰 업데이트
        mViewModel.viewItems.observe(viewLifecycleOwner, {
            mAdapter.submitList(it)
        })

        // 에러 처리
        mViewModel.liveFieldOf(RecordFilesFragmentState::error).observe(viewLifecycleOwner) { error ->
            if (error != null) {
                mBind.root.snack(error.message)
            }
        }
    }


    private fun setupRecyclerView() {
        mAdapter = buildListAdapter(mBind.recyclerView, spanCount = 1) {
            addViewType(RecordFileItem::class)
        }

        mAdapter.apply {
            registerOnBindBefore(RecordFileItem::class) { item ->
                item.onHolderClick = ::onHolderClickRecordFile
                item.onDownloadClick = ::onHolderClickRecordFileCheck
                item.callbackIsEditing = ::holderIsEditing
            }
        }

        mAdapter.holderTracker = object : DefaultHolderTracker() {
            override fun holderSelected(item: Any): Boolean {
                val fileId = (item as? RecordFileItem)?.recordFile?.fileId ?: return false
                val state = mViewModel.currentState()
                return state.isEditing && state.checkedFileIds.contains(fileId)
            }
        }

        //mBind.recyclerView.setHasFixedSize(true)
        // mBind.recyclerView.addItemDecoration(mGridItemDecoration)
    }


    private fun onHolderClickRecordFile(view: View, item: RecordFileItem) {
        val recordFile = item.recordFile
        val state = mViewModel.currentState()
        if (state.isEditing) {
            val selected = mViewModel.currentState().checkedFileIds.contains(recordFile.fileId)
            mViewModel.submitAction(RecordFilesFragmentAction.ToggleSelection(recordFile.fileId))
            item.updateSelection(!selected)
        } else {
            val cameraIp = mViewModel.camManager.cameraIp
            if (cameraIp != null) {
                startActivity(
                    FilePlayerActivity.createIntent(
                        context = requireContext(),
//                    uri = Uri.parse("https://ohlab.kr/p/kuk/sample/stevejobs.mp4")
                        // uri = Uri.parse("http://192.168.0.100/recording/download?fileName=19700101_103208_1920x1080_15_1500_30000_7434536.mp4")
                        uri = Cam.recordFileUrl(ip = cameraIp, fileId = recordFile.fileId).toUri()
                    )
                )
            }
        }
    }

    private fun onHolderClickRecordFileCheck(view: View, item: RecordFileItem) {
        lifecycleScope.launch {
            openRecordFileDownload(recordFile = item.recordFile)
        }

    }

    private suspend fun askPermission(permission: PermissionConfig): Boolean {
        // 권한 요청하기
        val result = permission.request(this)
        if (result is TRPermissionResult.PermissionGranted) {
            return true
        }

        // 권한을 다시보지 않기로 거부한 경우 설정화면으로 가겠냐고 물어보기
        if (result is TRPermissionResult.PermissionDeniedPermanently) {
            confirmOk(permission.gotoSettingMsgResId) {
                if (it) {
                    PermissionUtils.openAppSettingActivity(this)
                }
            }
        }
        return false
    }

    private suspend fun openRecordFileDownload(recordFile: KuRecordFile) {
        val cfg = mViewModel.camManager.config ?: return
        val ip = mViewModel.camManager.cameraIp ?: return
        if (!askPermission(PermissionConfig.WRITE_EXTERNAL_STORAGE)) {
            return
        }

        CameraDialogs.openRecordFileDownload(
            fm = childFragmentManager,
            fileId = recordFile.fileId,
        ) {
            Timber.d("download dialog closed")
        }
    }

    // 편집모드 여부
    private fun holderIsEditing(item: RecordFileItem): Boolean {
        return mViewModel.currentState().isEditing
    }

    /**
     * 백키 핸들러 - 정렬 인덱싱이 표시중이라면 제거
     */
    private val editingCancelBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            mViewModel.submitAction(RecordFilesFragmentAction.ToggleEditing)
        }
    }


    /**
     * 백키 핸들러 - 필터 취소 제거
     */
    private val filterCancelBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            mViewModel.submitAction(RecordFilesFragmentAction.SetFilter(null))
        }
    }
}
