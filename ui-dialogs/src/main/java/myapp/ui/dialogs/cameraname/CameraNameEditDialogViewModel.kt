package myapp.ui.dialogs.cameraname

import android.content.Context
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import myapp.ReduxViewModel
import myapp.util.Logger
import myapp.util.ObservableLoadingCounter
import javax.inject.Inject


internal data class CameraNameEditDialogState(
    val dummy: Long = 0,
)

@HiltViewModel
internal class CameraNameEditDialogViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val logger: Logger,
    // private val bibleCardMessageSave: BibleCardMessageSave,
) : ReduxViewModel<CameraNameEditDialogState>(CameraNameEditDialogState()) {
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

