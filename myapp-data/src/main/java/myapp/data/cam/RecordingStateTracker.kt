package myapp.data.cam

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import myapp.data.code.CamConnectivity
import myapp.flowInterval
import timber.log.Timber
import javax.inject.Inject


class RecordingStateTracker @Inject constructor(
    val context: Context,
) : DefaultLifecycleObserver {

    private var checkIntervalJob: Job? = null
    private var registered = false
    private var checking = false
    private var lastCheckTime = 0L
    val connectionFlow = MutableStateFlow(CamConnectivity.NOT_CONNECTED)

    private val checkIntervalFlow = flowInterval(3000, CONNECTIVITY_CHECK_INTERVAL)


    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) {
        start()
    }

    override fun onPause(owner: LifecycleOwner) {
        stop()
    }

    private fun checkConnectivity(force: Boolean = false) {
        if (!registered) return
        if (checking) return
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            try {
                checkConnectivityInternal(force)
            } finally {
                checking = false
            }
        }
    }

    private suspend fun checkConnectivityInternal(force: Boolean) {
        if (!force) {
            val diff = System.currentTimeMillis() - lastCheckTime
            if (diff < CONNECTIVITY_CHECK_INTERVAL && connectionFlow.value == CamConnectivity.CONNECTED) {
                Timber.i("XXX SKIP CHECK")
                return
            }
        }

        lastCheckTime = System.currentTimeMillis()

    }

    private fun start() {
        if (registered) return
        registered = true
        checkIntervalJob = ProcessLifecycleOwner.get().lifecycleScope.launch {
            checkIntervalFlow.collect {
                checkConnectivity()
            }
        }
    }

    private fun stop() {
        if (!registered) return
        registered = false
        checkIntervalJob?.cancel()
        checkIntervalJob = null
    }

    protected fun finalize() {
        stop()
    }

    companion object {
        private const val CONNECTIVITY_CHECK_INTERVAL = 3000L
    }
}
