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

data class FolderCount(
    val count: Int,
    val lastUpdated: Long = System.currentTimeMillis()
)

interface IFolderRepository {
    val currentFolder: StateFlow<Folder?>
    suspend fun getCurrentFolder(): Folder?
    suspend fun getFolderCount(): FolderCount
    fun observeFolderCount(): Flow<FolderCount>
    suspend fun refreshFolderCount()
    fun setCurrentFolder(folder: Folder)
}

class FolderRepository(
    private val settings: AppSettings,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IFolderRepository {
    private val _currentFolder = MutableStateFlow<Folder?>(null)
    override val currentFolder: StateFlow<Folder?> = _currentFolder.asStateFlow()

    private val _folderCount = MutableStateFlow<FolderCount>(FolderCount(0))

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
        folderDataSource.countFolders().also { count ->
            _folderCount.emit(FolderCount(count))
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

        val folder = Folder.getAll().firstOrNull { it.id == folderId }
        cache = CacheEntry(folder, folderId)
        _currentFolder.value = folder
    }

    override suspend fun getCurrentFolder(): Folder? {
        return Folder.getAll().firstOrNull { it.id == settings.currentFolderId }
    }

    override fun setCurrentFolder(folder: Folder) {
        settings.currentFolderId = folder.id
        _currentFolder.value = folder
    }
}