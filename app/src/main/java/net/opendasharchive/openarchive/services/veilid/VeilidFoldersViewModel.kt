package net.opendasharchive.openarchive.services.veilid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.Folder

class VeilidFoldersViewModel : ViewModel() {
    companion object {
        private val MOCK_FOLDERS = listOf(
            Folder("Veilid Folder 1", backend = Backend(Backend.Type.VEILID)),
            Folder("Veilid Folder 2", backend = Backend(Backend.Type.VEILID)),
            Folder("Veilid Folder 3", backend = Backend(Backend.Type.VEILID))
        )
    }

    private val _folders = MutableLiveData<List<Folder>>()
    val groups: LiveData<List<Folder>> = _folders

    init {
        _folders.value = MOCK_FOLDERS
    }

    fun getItemAtPosition(position: Int): Folder? {
        return _folders.value?.getOrNull(position)
    }
}