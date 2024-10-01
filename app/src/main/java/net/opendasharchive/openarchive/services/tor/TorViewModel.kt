
import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import net.opendasharchive.openarchive.services.tor.TorForegroundService
import net.opendasharchive.openarchive.services.tor.TorRepository
import net.opendasharchive.openarchive.services.tor.TorStatus
import net.opendasharchive.openarchive.util.Prefs

class TorViewModel(
    application: Application,
    private val torRepository: TorRepository
) : AndroidViewModel(application) {

    val torStatus: StateFlow<TorStatus> = torRepository.torStatus

    fun updateTorServiceState() {
        if (Prefs.useTor) {
            startTor()
        } else {
            stopTor()
        }
    }

    private fun startTor() {
        Intent(getApplication(), TorForegroundService::class.java).also { intent ->
            getApplication<Application>().startForegroundService(intent)
        }
    }

    private fun stopTor() {
        Intent(getApplication(), TorForegroundService::class.java).also { intent ->
            getApplication<Application>().stopService(intent)
        }
    }
}