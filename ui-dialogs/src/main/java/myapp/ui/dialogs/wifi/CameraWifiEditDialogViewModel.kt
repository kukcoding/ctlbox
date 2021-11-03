package myapp.ui.dialogs.wifi

import android.content.Context
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import myapp.ReduxViewModel
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal data class CameraWifiEditDialogState(
    val dummy: Long = 0,
)

@HiltViewModel
internal class CameraWifiEditDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logger: Logger,
    // private val bibleCardMessageSave: BibleCardMessageSave,
) : ReduxViewModel<CameraWifiEditDialogState>(CameraWifiEditDialogState()) {
    private val loadingState = ObservableLoadingCounter()

    // for data binding
    val isLoadingLive = loadingState.observable.asLiveData()

    /**
     * 카드 메시지 저장
     */
    suspend fun saveBibleCardMessage(cardId: Long, cardMessage: CharSequence?): String {
        loadingState.addLoader()
        return try {
            // bibleCardMessageSave.executeSync(cardId = cardId, cardMessage = cardMessage)
            ""
        } finally {
            loadingState.removeLoader()
        }
    }
}

