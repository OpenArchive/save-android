package net.opendasharchive.openarchive.features.folders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.IFolderRepository

class FolderViewModel(
    private val folderRepository: IFolderRepository
) : ViewModel() {
    private val _currentFolder = MutableStateFlow<UiState<Folder?>>(UiState.Loading)
    val currentFolder = _currentFolder.asStateFlow()

    init {
        loadCurrentFolder()
    }

    private fun loadCurrentFolder() {
        viewModelScope.launch {
            _currentFolder.value = UiState.Loading
            folderRepository.getCurrentFolder()
                .fold(
                    onSuccess = { folder -> _currentFolder.value = UiState.Success(folder) },
                    onFailure = { error -> _currentFolder.value = UiState.Error(error) }
                )
        }
    }
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: Throwable) : UiState<Nothing>
}