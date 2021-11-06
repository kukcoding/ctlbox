package myapp.ui.dialogs.recordfilefilter

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kr.ohlab.android.recyclerviewgroup.ItemBase
import myapp.ReduxViewModel
import myapp.data.entities.RecordFileFilter
import myapp.ui.dialogs.recordfilefilter.viewholder.RecordFileFilterItem
import myapp.util.generateNumericIdProvider
import javax.inject.Inject


internal sealed class RecordFileFiltersAction {
    data class SelectFilter(val filter: RecordFileFilter?) : RecordFileFiltersAction()
}

internal data class RecordFileFiltersViewState(
    val selectedFilter: RecordFileFilter? = null
)


@HiltViewModel
internal class RecordFileFiltersDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ReduxViewModel<RecordFileFiltersViewState>(
    RecordFileFiltersViewState(selectedFilter = null)
) {
    private val pendingActions = Channel<RecordFileFiltersAction>(Channel.BUFFERED)
    private val toNumId = generateNumericIdProvider()

    val viewItems = MutableStateFlow(emptyList<ItemBase<*>>())

    init {
        // 액션 처리
        viewModelScope.launch {
            pendingActions.consumeAsFlow().collect { action ->
                when (action) {
                    is RecordFileFiltersAction.SelectFilter -> {
                        setState {
                            copy(
                                selectedFilter = action.filter
                            )
                        }
                    }
                }
            }
        }
    }


    fun submitAction(vararg actions: RecordFileFiltersAction) {
        viewModelScope.launch {
            if (!pendingActions.isClosedForSend) {
                actions.forEach { pendingActions.send(it) }
            }
        }
    }

    fun replaceFileFilters(recordFileFilters: List<RecordFileFilter>) {
        this.viewItems.value = recordFileFilters.map { RecordFileFilterItem(filter = it, id = toNumId(it.key)) }
    }
}

