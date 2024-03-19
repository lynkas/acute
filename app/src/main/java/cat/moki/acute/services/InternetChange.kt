package cat.moki.acute.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.ContextCompat

fun registerInternetChange(
    context: Context,
    onChange: (internet: Boolean, metered: Boolean) -> Unit = { _: Boolean, _: Boolean -> },
    onAvailable: (internet: Boolean, metered: Boolean) -> Unit = { _: Boolean, _: Boolean -> },
    onCapabilitiesChanged: (internet: Boolean, metered: Boolean) -> Unit = { _: Boolean, _: Boolean -> },
    onLost: (internet: Boolean, metered: Boolean) -> Unit = { _: Boolean, _: Boolean -> },
) {
    val networkRequest = NetworkRequest.Builder()
        .build()
    val connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager::class.java) as ConnectivityManager
    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        // network is available for use
        override fun onAvailable(network: Network) {

            super.onAvailable(network)
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            networkCapabilities?.let {
                val internet = it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val metered = !it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                onAvailable(internet, metered)
                onChange(internet, metered)
            }

        }

        // Network capabilities have changed for the network
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val internet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val metered = !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            onCapabilitiesChanged(internet, metered)
            onChange(internet, metered)

        }

        // lost network connection
        override fun onLost(network: Network) {
            super.onLost(network)
            onLost(false, true)
            onChange(false, true)
        }
    }
    connectivityManager.requestNetwork(networkRequest, networkCallback)
}