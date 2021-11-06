package myapp.data.cam

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.lifecycle.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import myapp.SingletonHolder
import myapp.data.code.CamConnectivity
import myapp.flowInterval
import myapp.util.NetworkUtils
import splitties.init.appCtx
import timber.log.Timber


class CamConnectMonitor(private val context: Context) : DefaultLifecycleObserver {
    private var checkIntervalJob: Job? = null
    private var registered = false
    private var checking = false
    private var lastConnectivityCheckTime = 0L
    val connectionFlow = MutableStateFlow(CamConnectivity.NOT_CONNECTED)

    private val checkIntervalFlow = flowInterval(3000, CONNECTIVITY_CHECK_INTERVAL)

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (registered) {
                checkConnectivity(force = true)
            }
        }
    }


    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this@CamConnectMonitor)
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
            val diff = System.currentTimeMillis() - lastConnectivityCheckTime
            if (diff < CONNECTIVITY_CHECK_INTERVAL && connectionFlow.value == CamConnectivity.CONNECTED) {
                Timber.i("XXX SKIP CHECK")
                return
            }
        }

        lastConnectivityCheckTime = System.currentTimeMillis()
        if (!NetworkUtils.isWifiEnabled(appCtx)) {
            connectionFlow.tryEmit(CamConnectivity.DISABLED)
            return
        } else {
            Timber.i("XXX CHECK CONNECTABLE")
//            val connectable = Cam.checkConnectable()
//            val newState = if (connectable) CamConnectivity.CONNECTED else CamConnectivity.NOT_CONNECTED
//            connectionFlow.tryEmit(newState)
        }
    }

    private fun start() {
        if (registered) return
        registered = true
        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED")
        filter.addAction("android.net.wifi.STATE_CHANGE")
        context.registerReceiver(receiver, filter)

        checkIntervalJob = ProcessLifecycleOwner.get().lifecycleScope.launch {
            checkIntervalFlow.collect {
                checkConnectivity()
            }
        }
    }

    private fun stop() {
        if (!registered) return
        registered = false
        context.unregisterReceiver(receiver)
        checkIntervalJob?.cancel()
        checkIntervalJob = null
    }

    fun updateConnectivityOkForce() {
        lastConnectivityCheckTime = System.currentTimeMillis()
        connectionFlow.tryEmit(CamConnectivity.CONNECTED)
    }

    protected fun finalize() {
        stop()
    }

    companion object : SingletonHolder<CamConnectMonitor, Context>({ CamConnectMonitor(it.applicationContext) }) {
        private const val CONNECTIVITY_CHECK_INTERVAL = 3000L
    }
}

