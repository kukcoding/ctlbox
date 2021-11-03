package myapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.*
import timber.log.Timber

@Deprecated("Use ConnectivityFlow")
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

class ConnectivityFlow constructor(
    @ApplicationContext val context: Context
) {
    fun observeNetworkState(debounceMilli: Long = 500): Flow<NetworkUtils.NetworkState> {
        return observeConnectivityChanged()
            .debounce(debounceMilli)
            .map { NetworkUtils.getNetworkState(context) }
            .distinctUntilChanged()
    }

    fun observeInternetAvailable(debounceMilli: Long = 500): Flow<Boolean> {
        return observeConnectivityChanged()
            .debounce(debounceMilli)
            .map { NetworkUtils.hasInternet(context) }
            .distinctUntilChanged()
    }

    private fun observeConnectivityChanged() = callbackFlow {
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = OnNetworkChangedCallback {
            sendBlocking(System.currentTimeMillis())
        }
        mgr.registerNetworkCallback(buildNetworkRequest(), callback)
        awaitClose {
            mgr.unregisterNetworkCallback(callback)
        }
    }

    private fun buildNetworkRequest() = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private class OnNetworkChangedCallback(val block: () -> Unit) : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            Timber.d("XXX onLost: $network")
            block()
        }

        override fun onUnavailable() {
            super.onUnavailable()
            Timber.d("XXX onUnavailable")
            block()
        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Timber.d("XXX onAvailable")
            block()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            block()
        }
    }
}
