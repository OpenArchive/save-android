
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.services.tor.TorForegroundService
import net.opendasharchive.openarchive.services.tor.TorStatus
import net.opendasharchive.openarchive.util.Prefs

class TorViewModel(application: Application) : AndroidViewModel(application) {
    private val _torStatus = MutableStateFlow<TorStatus>(TorStatus.DISCONNECTED)

    init {
        updateTorServiceState()
    }

    fun updateTorServiceState() {
        if (Prefs.useTor) {
            startTor()
        } else {
            stopTor()
        }
    }

    fun startTor() {
        _torStatus.value = TorStatus.CONNECTING
        Intent(getApplication(), TorForegroundService::class.java).also { intent ->
            getApplication<Application>().startForegroundService(intent)
        }

        viewModelScope.launch {
            delay(2000) // This is a placeholder. In reality, you'd want a way to know when Tor is actually ready
            _torStatus.value = TorStatus.CONNECTED
        }
    }

    fun stopTor() {
        Intent(getApplication(), TorForegroundService::class.java).also { intent ->
            getApplication<Application>().stopService(intent)
        }
        _torStatus.value = TorStatus.DISCONNECTED
    }
}