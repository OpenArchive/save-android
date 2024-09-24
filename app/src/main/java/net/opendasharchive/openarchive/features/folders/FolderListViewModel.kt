package net.opendasharchive.openarchive.features.folders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.Folder

class FolderListViewModel(backend: Backend?) : ViewModel() {
    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>> = _folders

    init {
        _folders.value = Folder.getLocalFoldersForBackend(backend)
    }
}