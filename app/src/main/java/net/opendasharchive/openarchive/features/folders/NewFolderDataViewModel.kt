package net.opendasharchive.openarchive.features.folders

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.opendasharchive.openarchive.db.Folder

class NewFolderDataViewModel : ViewModel() {
    private val _folder = MutableStateFlow(Folder())
    val folder: StateFlow<Folder> = _folder.asStateFlow()

    fun updateBackendLicense(license: String) {
        _folder.update { folder ->
            folder.copy(
                backend = folder.backend?.copy(licenseUrl = license)
            )
        }
    }

    fun updateBackendNickname(nickname: String) {
        _folder.update { folder ->
            folder.copy(
                backend = folder.backend?.copy(name = nickname)
            )
        }
    }

    fun updateFolder(update: (Folder) -> Folder) {
        _folder.update(update)
    }
}