package myapp.ui.record

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.ohlab.android.recyclerviewgroup.ItemBase
import myapp.ReduxViewModel
import myapp.api.UiError
import myapp.data.cam.CamManager
import myapp.data.entities.RecordFileFilter
import myapp.domain.interactors.LoadRecordFiles
import myapp.domain.interactors.RemoveRecordFile
import myapp.ui.SnackbarManager
import myapp.ui.record.viewholder.RecordFileItem
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import myapp.util.generateNumericIdProvider
import myapp.util.tupleOf
import javax.inject.Inject


internal sealed class RecordFilesFragmentAction {
    object Refresh : RecordFilesFragmentAction()
    object ToggleEditing : RecordFilesFragmentAction()
    data class ToggleSelection(val fileId: String) : RecordFilesFragmentAction()
    data class RemoveViewItemForce(val fileId: String) : RecordFilesFragmentAction()
    data class SetFilter(val filter: RecordFileFilter?) : RecordFilesFragmentAction()
    object SelectAll : RecordFilesFragmentAction()
    object ClearAllSelection : RecordFilesFragmentAction()
}


internal data class RecordFilesFragmentState(
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val isDeleting: Boolean = false,
    val filter: RecordFileFilter? = null,
    val error: UiError? = null,
    val checkedFileIds: Set<String> = emptySet(),
)

@HiltViewModel
internal class RecordFilesFragmentViewModel @Inject constructor(
    private val loadRecordFiles: LoadRecordFiles,
    private val removeRecordFile: RemoveRecordFile,
    private val snackbarManager: SnackbarManager,
    val camManager: CamManager,
    private val logger: Logger
) : ReduxViewModel<RecordFilesFragmentState>(RecordFilesFragmentState()) {
    private val loadingState = ObservableLoadingCounter()
    val removingState = ObservableLoadingCounter()
    private val pendingActions = Channel<RecordFilesFragmentAction>(Channel.BUFFERED)
    private val toNumId = generateNumericIdProvider()

    val originalViewItems = MutableLiveData<List<ItemBase<*>>>()

    //val viewItems = MutableLiveData<List<ItemBase<*>>>()
    val viewItems: LiveData<List<ItemBase<*>>> = combine(
        originalViewItems.asFlow(),
        flowFieldOf(RecordFilesFragmentState::filter),
        ::tupleOf
    ).flatMapLatest { (originalViewItems, filter) ->
        flowOf(updateFilter(originalViewItems, filter))
    }.asLiveData()

    val isEditingLive = liveFieldOf(RecordFilesFragmentState::isEditing)
    val isEmptyLive = viewItems.map { it.isEmpty() }
    val isLoadingLive = liveFieldOf(RecordFilesFragmentState::isLoading)
    val isFilterOnLive = liveFieldOf(RecordFilesFragmentState::filter).map { it != null }
    val filterTextLive = liveFieldOf(RecordFilesFragmentState::filter).map {
        if (it == null) {
            ""
        } else {
            "${it.year}년 ${it.monthValue}월 ${it.dayOfMonth}일 ${it.hour}시"
        }
    }

    // 로딩중에는 empty view를 표시하지 않는다
    val isEmptyVisibleLive = combine(
        isEmptyLive.asFlow(),
        flowFieldOf(RecordFilesFragmentState::isLoading),
        flowFieldOf(RecordFilesFragmentState::isDeleting),
        ::tupleOf
    ).map { (empty, loading) -> empty && !loading }.asLiveData()


    val isProgressBarVisibleLive = combine(
        flowFieldOf(RecordFilesFragmentState::isLoading),
        flowFieldOf(RecordFilesFragmentState::isDeleting),
        ::tupleOf
    ).map { (loading, deleting) -> loading || deleting }.asLiveData()

    val fileCountTextLive = viewItems.map { "${it.size} Files" }
    val fileCountLive = viewItems.map { it.size }

    val selectionCountTextLive = liveFieldOf(RecordFilesFragmentState::checkedFileIds).map { it.size.toString() }
    val selectionCountLive: LiveData<Int> = liveFieldOf(RecordFilesFragmentState::checkedFileIds).map { it.size }

    val cameraNameLive = camManager.observeConfig().asLiveData().map { it?.cameraName ?: "" }

    init {
        // 스낵바 에러 처리
        snackbarManager.launchInScope(viewModelScope) { uiError, visible ->
            viewModelScope.launchSetState {
                copy(error = if (visible) uiError else null)
            }
        }

        // 로딩 상태 관찰
        viewModelScope.launch {
            loadingState.observable.collectAndSetState { loading ->
                copy(isLoading = loading)
            }
        }


        // 삭제 상태 관찰
        viewModelScope.launch {
            removingState.observable.collectAndSetState { deleting ->
                copy(isDeleting = deleting)
            }
        }

        // 새로고침
        viewModelScope.launch {
            submitAction(RecordFilesFragmentAction.Refresh)
        }

        // 액션 처리
        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is RecordFilesFragmentAction.Refresh -> refresh()
                    is RecordFilesFragmentAction.ToggleEditing -> toggleEditing()
                    is RecordFilesFragmentAction.ToggleSelection -> toggleSelection(fileId = action.fileId)
                    is RecordFilesFragmentAction.SelectAll -> selectAll()
                    is RecordFilesFragmentAction.ClearAllSelection -> setState { copy(checkedFileIds = emptySet()) }
                    is RecordFilesFragmentAction.RemoveViewItemForce -> removeViewItemForce(fileId = action.fileId)
                    is RecordFilesFragmentAction.SetFilter -> setState { copy(filter = action.filter) }
                }
            }
        }
    }

    fun setupWithLifecycle(lifecycleScope: LifecycleCoroutineScope) {
    }

    fun submitAction(action: RecordFilesFragmentAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                pendingActions.send(action)
            }
        }
    }


    private suspend fun updateFilter(itemList: List<ItemBase<*>>, filter: RecordFileFilter?): List<ItemBase<*>> {
        val items = itemList.filterIsInstance<RecordFileItem>()
        if (filter == null || items.isNullOrEmpty()) {
            return itemList
        }
        val filterKey = filter.key
        return withContext(Dispatchers.Default) {
            items.filter {
                it.recordFile.yyyymmddhhmiss.startsWith(filterKey)
            }
        }
    }

    private suspend fun refresh() {
        val cameraIp = camManager.cameraIp ?: return
        loadingState.addLoader()
        try {
            originalViewItems.value = withContext(Dispatchers.Default) {
                val files = loadRecordFiles.executeSync(Unit).map {
                    RecordFileItem(cameraIp = cameraIp, recordFile = it, id = toNumId(it.fileId))
                }
                files
            } ?: emptyList()
        } finally {
            loadingState.removeLoader()
        }
    }

    private fun toggleEditing() {
        viewModelScope.launch {
            val newEditing = !currentState().isEditing
            if (newEditing) {
                setState {
                    copy(
                        isEditing = newEditing,
                        checkedFileIds = emptySet(),
                        filter = null
                    )
                }
            } else {
                setState {
                    copy(
                        isEditing = newEditing,
                        checkedFileIds = emptySet()
                    )
                }
            }
        }
    }

    private suspend fun toggleSelection(fileId: String) {
        val currSet = currentState().checkedFileIds
        if (currSet.contains(fileId)) {
            withContext(Dispatchers.Default) {
                setState {
                    copy(checkedFileIds = currSet - fileId)
                }
            }
        } else {
            withContext(Dispatchers.Default) {
                setState {
                    copy(checkedFileIds = currSet + fileId)
                }
            }
        }
    }

    private suspend fun removeViewItemForce(fileId: String) {
        val items = this.originalViewItems.value ?: return

        this.originalViewItems.value = withContext(Dispatchers.Default) {
            items.mapNotNull {
                if (it is RecordFileItem && it.recordFile.fileId == fileId) {
                    null
                } else {
                    it
                }
            }
        }
        setState { copy(checkedFileIds = checkedFileIds - fileId) }
    }

    private suspend fun selectAll() {
        withContext(Dispatchers.Default) {
            val allFileIds = recordFileItems().map { it.recordFile.fileId }
            setState {
                copy(checkedFileIds = allFileIds.toSet())
            }
        }
    }

    suspend fun removeRecordFile(fileId: String) {
        removeRecordFile.executeSync(fileId = fileId)
    }

    suspend fun createRecordFileFilters(): List<RecordFileFilter> {
        return withContext(Dispatchers.Default) {
            val result = mutableMapOf<String, Int>()
            val files = recordFileItems().map { it.recordFile }
            files.forEach { f ->
                val cnt = result[f.filterKey] ?: 0
                result[f.filterKey] = cnt + 1
            }
            result.map { entry ->
                val ymdh = entry.key
                RecordFileFilter(
                    year = ymdh.substring(0, 4).toInt(),
                    monthValue = ymdh.substring(4, 6).toInt(),
                    dayOfMonth = ymdh.substring(6, 8).toInt(),
                    hour = ymdh.substring(8, 10).toInt(),
                    count = entry.value
                )
            }
        }
    }

    private fun recordFileItems(): List<RecordFileItem> {
        return originalViewItems.value?.filterIsInstance<RecordFileItem>() ?: emptyList()
    }
}
