package myapp.data.cam

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import myapp.SingletonHolder
import myapp.flowInterval
import myapp.util.NetworkUtils
import timber.log.Timber


class WifiStatusTracker(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private sealed class WifiState {
        object Enabled : WifiState()
        object Disabled : WifiState()
    }

    private val networkStateFlow = callbackFlow<WifiState> {
        val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onUnavailable() {
                trySend(WifiState.Disabled)
            }

            override fun onAvailable(network: Network) {
                trySend(WifiState.Enabled)
            }

            override fun onLost(network: Network) {
                trySend(WifiState.Disabled)
            }
        }

        Timber.d("registerNetworkCallback for wifi state tracker")
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        connectivityManager.registerNetworkCallback(request, networkStatusCallback)

        awaitClose {
            Timber.d("unregisterNetworkCallback for wifi state tracker")
            connectivityManager.unregisterNetworkCallback(networkStatusCallback)
        }
    }

    val flow: Flow<Boolean> = networkStateFlow.flatMapLatest {
        if (it is WifiState.Enabled) {
            val enabled = NetworkUtils.isWifiEnabled(context)
            if (enabled) {
                flowOf(true)
            } else {
                // enable 에서 바로 WIFI가 enable 안된 경우도 있다.
                // 계속 반복해서 체크한다
                flowInterval(500, 500)
                    .filter { NetworkUtils.isWifiEnabled(context) }
                    .take(1)
                    .map { true }
            }
        } else {
            flowOf(false)
        }
    }.distinctUntilChanged()


    companion object : SingletonHolder<WifiStatusTracker, Context>({ WifiStatusTracker(it.applicationContext) })
}

