package myapp.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TwiceBackPressedCallback(
    val context: Context,
    val lifecycleScope: LifecycleCoroutineScope,
    val message: String,
) : OnBackPressedCallback(true) {

    companion object {
        // 백키 눌렀을때 한번 더 눌러야 종료되도록
        private const val DELAY_FINISH_TIMEOUT_MILLIS = 2 * 1000
    }

    private var mLastBackKeyPressedTime = 0L
    private var autoCancelJob: Job? = null
    private var toast: Toast? = null

    override fun handleOnBackPressed() {
        val now = System.currentTimeMillis()
        if (now - mLastBackKeyPressedTime > DELAY_FINISH_TIMEOUT_MILLIS) {
            mLastBackKeyPressedTime = now
            toast?.cancel()
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT).apply {
                show()
            }

            autoCancelJob?.cancel()
            autoCancelJob = lifecycleScope.launch {
                delay(DELAY_FINISH_TIMEOUT_MILLIS.toLong())
                isEnabled = true
            }
            isEnabled = false
            return
        }
    }
}
