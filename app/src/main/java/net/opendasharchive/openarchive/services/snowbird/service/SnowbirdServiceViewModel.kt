package net.opendasharchive.openarchive.services.snowbird.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SnowbirdServiceViewModel : ViewModel() {
    private val _serviceEvent = Channel<ServiceCommand>()
    val serviceEvent = _serviceEvent.receiveAsFlow()

    /**
     * Commands that can be sent to control the service lifecycle
     */
    sealed class ServiceCommand {
        data object Start : ServiceCommand()
        data object Stop : ServiceCommand()
    }

    /**
     * Initiates service start if needed
     */
    fun startService() {
        viewModelScope.launch {
            _serviceEvent.send(ServiceCommand.Start)
        }
    }

    /**
     * Initiates service shutdown
     */
    fun stopService() {
        viewModelScope.launch {
            _serviceEvent.send(ServiceCommand.Stop)
        }
    }
}