package myapp.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.provider.Settings
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.math.BigInteger
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

object NetworkUtils {

    /**
     * 비행기 모드 체크
     */
    fun isAirplaneMode(context: Context): Boolean {
        return Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
    }

    fun getLocalIp(context: Context): String? {
        return getWifiIp(context) ?: getNetworkInterfaceIp()
    }

    suspend fun fetchPublicIp(
        ipFetchUrl: String = "http://checkip.amazonaws.com/",
        timeoutSeconds: Long = 15 * 60
    ): String? {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient.Builder()
                .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .build()
            try {
                val response = client.newCall(Request.Builder().url(ipFetchUrl).build()).execute()
                val ip = response.body?.string()
                if (ip.isNullOrBlank()) null else ip
            } catch (ignore: IOException) {
                null
            }
        }
    }

    fun getWifiIp(context: Context): String? {
        val wm = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val ipAddress = if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            Integer.reverseBytes(wm.connectionInfo.ipAddress)
        } else {
            wm.connectionInfo.ipAddress
        }

        if (ipAddress == 0) {
            return null
        }

        val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
        return try {
            val ip = InetAddress.getByAddress(ipByteArray).hostAddress
            if (ip.isNullOrBlank()) null else ip
        } catch (ignore: Throwable) {
            // Timber.d(ignore, "ipByteArray.length=${ipByteArray.size}, wm.connectionInfo.ipAddress=${ipAddress}")
            null
        }
    }

    fun getNetworkInterfaceIp(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces().toList()
                .flatMap { it.inetAddresses.toList() }
                .filterIsInstance<Inet4Address>()
                .filter { !it.isLoopbackAddress && !it.isLinkLocalAddress }
                .map { it.hostAddress }
                .firstOrNull { !it.isNullOrBlank() }
        } catch (ignore: Exception) {
            return null
        }
    }


    //    fun hasInternet(context: Context): Boolean {
//        /* Taken from Johan's answer at: https://stackoverflow.com/a/35009615 */
//        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network: Network?
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            network = cm.activeNetwork
//        } else {
//            // TODO
//            return true
//        }
//        if (network != null) {
//            val capabilities = cm.getNetworkCapabilities(network)
//            return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
//        }
//    }

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun isActiveNetworkMetered(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.isActiveNetworkMetered
    }

    /**
     * 인터넷 사용 여부 체크
     * 나중에 위에 있는 함수로 사용하자
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun hasInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val net = cm.allNetworks.mapNotNull { cm.getNetworkCapabilities(it) }
            .firstOrNull { capabilities ->
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                //capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
        return net != null
    }

//
//    // 테스트는 못해봤다
//    fun hasNetwork(context: Context): Boolean {
//        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        val net = cm.allNetworks.mapNotNull { cm.getNetworkCapabilities(it) }
//            .firstOrNull { capabilities ->
//                //capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
//            }
//        return net != null
//    }

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun isConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }


    /**
     * 인터넷 사용 여부 체크
     * 나중에 위에 있는 함수로 사용하자
     */
    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun isWifiEnabled(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    data class NetworkState(
        val isMetered: Boolean,
        val isConnected: Boolean
    )

    @SuppressLint("MissingPermission")
    fun getNetworkState(context: Context): NetworkState {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        val isConnected =
            capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isMetered = cm.isActiveNetworkMetered
        return NetworkState(
            isConnected = isConnected,
            isMetered = isMetered
        )
    }
}
