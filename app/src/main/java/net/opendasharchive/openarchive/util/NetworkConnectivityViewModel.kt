package net.opendasharchive.openarchive.util

import android.app.Application
import android.content.Context

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NetworkConnectivityViewModel(application: Application) : AndroidViewModel(application) {
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkStatusLiveData = MutableLiveData<Boolean>()
    val networkStatusLiveData: LiveData<Boolean> = _networkStatusLiveData

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            val isWifi = networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            _networkStatusLiveData.postValue(isWifi)
        }

        override fun onLost(network: Network) {
            _networkStatusLiveData.postValue(false)
        }
    }

    init {
        registerNetworkCallback()
    }

    private fun registerNetworkCallback() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onCleared() {
        super.onCleared()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}