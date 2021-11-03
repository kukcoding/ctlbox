package myapp.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import timber.log.Timber

class NetworkStateChangedLiveData constructor(
    private val context: Context
) : LiveData<Long>() {

    private val connMgr: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallbacks = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            Timber.d("XXX onLost: $network")
            postValue(System.currentTimeMillis())
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Timber.d("XXX onUnavailable")
            postValue(System.currentTimeMillis())
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Timber.d("XXX onAvailable")
            postValue(System.currentTimeMillis())
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            postValue(System.currentTimeMillis())
        }
    }

    override fun onInactive() {
        super.onInactive()
        connMgr.unregisterNetworkCallback(networkCallbacks)
    }

    override fun onActive() {
        super.onActive()
        connMgr.registerNetworkCallback(buildNetworkRequest(), networkCallbacks)
    }

    private fun buildNetworkRequest() = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()
}
