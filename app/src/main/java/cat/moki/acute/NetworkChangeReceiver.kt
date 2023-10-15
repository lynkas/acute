package cat.moki.acute

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.ContextCompat.getSystemService
import cat.moki.acute.client.Client


class NetworkChangeReceiver : BroadcastReceiver() {

    private fun getConnectivityStatus(context: Context) {
        val connectivityManager =
            getSystemService(context, ConnectivityManager::class.java) as ConnectivityManager

        connectivityManager.requestNetwork(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build(), object : ConnectivityManager.NetworkCallback() {
                // network is available for use
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                }

                // Network capabilities have changed for the network
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    Client.networkMetered.value =
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                }

                // lost network connection
                override fun onLost(network: Network) {
                    super.onLost(network)
                }

            }
        )

    }

    override fun onReceive(context: Context, intent: Intent) {
        getConnectivityStatus(context)
    }
}