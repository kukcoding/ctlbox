package myapp.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.lifecycle.LiveData

class WifiStateChangedLiveData constructor(
    private val context: Context
) : LiveData<Long>() {

    override fun onInactive() {
        super.onInactive()
        context.unregisterReceiver(receiver)
    }

    override fun onActive() {
        super.onActive()

        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED")
        filter.addAction("android.net.wifi.STATE_CHANGE")
        context.registerReceiver(receiver, filter)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            postValue(System.currentTimeMillis())
        }
    }
}
