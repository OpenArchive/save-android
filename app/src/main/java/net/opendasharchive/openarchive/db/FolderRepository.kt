package net.opendasharchive.openarchive.db

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.opendasharchive.openarchive.util.AppSettings
import timber.log.Timber

data class FolderCount(
    val count: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

interface IFolderRepository {
    val currentFolder: StateFlow<Folder?>
    suspend fun getCurrentFolder(): Result<Folder?>
    suspend fun getFolderCount(): FolderCount
    fun observeFolderCount(): Flow<FolderCount>
    suspend fun refreshFolderCount()
    fun setCurrentFolder(folder: Folder)
}

class FolderRepository(
    private val settings: AppSettings,
    private val folderDataSource: IFolderDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IFolderRepository {
    private val _currentFolder = MutableStateFlow<Folder?>(null)
    override val currentFolder: StateFlow<Folder?> = _currentFolder.asStateFlow()

    private val _folderCount = MutableStateFlow<FolderCount>(FolderCount(0))

    private val _folderLoadError = MutableStateFlow<String?>(null)
    val folderLoadError: StateFlow<String?> = _folderLoadError.asStateFlow()

    private data class CacheEntry(
        val folder: Folder?,
        val folderId: Long,
        val timestamp: Long = System.currentTimeMillis()
    )

    private var cache: CacheEntry? = null
    private val cacheTimeout = 5_000L

    init {
        CoroutineScope(dispatcher).launch {
            settings.currentFolderIdFlow.collect { newId ->
                loadCurrentFolder(newId)
            }
        }
    }

    override fun observeFolderCount(): Flow<FolderCount> = _folderCount.asStateFlow()

    override suspend fun getFolderCount(): FolderCount = withContext(dispatcher) {
        folderDataSource.getFolderCount()
            .getOrDefault(0)
            .let { count ->
                FolderCount(count).also {
                    _folderCount.emit(it)
                }
            }
    }

    override suspend fun refreshFolderCount() {
        getFolderCount()
    }

    private suspend fun loadCurrentFolder(folderId: Long) {
        cache?.let { cached ->
            if (cached.folderId == folderId &&
                System.currentTimeMillis() - cached.timestamp < cacheTimeout) {
                _currentFolder.value = cached.folder
                return
            }
        }

        // Cache miss or expired - fetch from data source
        folderDataSource.getFolder(folderId)
            .onSuccess { folder ->
                // Update cache and current folder state
                folder?.let {
                    cache = CacheEntry(it, folderId)
                    _currentFolder.value = it
                } ?: run {
                    // Handle case where folder wasn't found
                    cache = null
                    _currentFolder.value = null
                    Timber.w("No folder found for ID: $folderId")
                }
            }
            .onFailure { error ->
                // Handle database errors while preserving last known state
                Timber.e(error, "Failed to load folder: $folderId")
                // Optionally notify UI of error through a separate state
                _folderLoadError.value = error.message ?: "Failed to load folder"
            }
    }

    override suspend fun getCurrentFolder(): Result<Folder?> = withContext(dispatcher) {
        if (settings.currentFolderId == -1L) {
            return@withContext Result.success(null)
        }

        folderDataSource.getFolder(settings.currentFolderId)
    }

    override fun setCurrentFolder(folder: Folder) {
        settings.currentFolderId = folder.id
        _currentFolder.value = folder
    }
}