package net.opendasharchive.openarchive.features.folders

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.services.SaveClient
import net.opendasharchive.openarchive.services.gdrive.GDriveConduit
import timber.log.Timber
import java.util.Date

class BrowseFoldersViewModel : ViewModel() {

    private val _items = MutableLiveData<List<ListItem>>()
    val items: LiveData<List<ListItem>> = _items

    val progressBarFlag = MutableLiveData(false)

    fun loadData(context: Context, forceLoad: Boolean = false, showLoadingIndicator: Boolean = true) {
        viewModelScope.launch {

            if (showLoadingIndicator) {
                progressBarFlag.value = true
            }

            try {
                val allFolders = mutableSetOf<ListItem>()

                val value = withContext(Dispatchers.IO) {
                    Timber.d("Copying down all backend folders (if necessary")

                    for (backend in Backend.getAll()) {
                        syncBackend(context, backend, forceLoad)
                    }

                    for (backend in Backend.getAll()) {
                        Timber.d("Loading local folders for ${backend.friendlyName}")

                        val someLocalFolders = getLocalFolders(backend)

                        allFolders.addAll(someLocalFolders)
                    }

                    allFolders.toList()
                }

                _items.value = value
            } catch (e: Error) {
                _items.value = emptyList()
                Timber.e(e)
                throw(e)
            } finally {
                if (showLoadingIndicator) {
                    progressBarFlag.value = false
                }
            }
        }
    }

    private fun getLocalFolders(backend: Backend): List<ListItem> {
        val folders = Folder.getLocalFoldersForBackend(backend).sortedByDescending { it.created }.map { ListItem.ContentItem(it) }

        if (folders.isNotEmpty()) {
            val allItems = mutableListOf<ListItem>()
            allItems.add(ListItem.SectionHeader(backend))
            allItems.addAll(folders)
            return allItems
        }

        return emptyList()
    }

    private fun syncGDrive(context: Context, backend: Backend) {
        GDriveConduit.listFoldersInRoot(GDriveConduit.getDrive(context), backend).forEach { folder ->
            if (folder.doesNotExist()) {
                Timber.d("Syncing ${folder.name}")
                folder.save()
            }
        }
    }

    private suspend fun syncWebDav(context: Context, backend: Backend) {
        val root = backend.hostUrl?.encodedPath

        SaveClient.getSardine(context, backend).list(backend.host)?.mapNotNull {
            if (it.isDirectory && it.path != root) {
                val folder = Folder(
                    name = it.name,
                    backend = backend,
                    created = it.modified ?: Date()
                )

                if (folder.doesNotExist()) {
                    Timber.d("Syncing ${folder.name}")
                    folder.save()
                }
            }
        }
    }

    private suspend fun syncBackend(context: Context, backend: Backend, forceLoad: Boolean = false) {
        if (!forceLoad && !backend.shouldSync()) {
            return
        }

        Timber.d("Syncing folders for ${backend.friendlyName}")

        when (backend.tType) {
            Backend.Type.WEBDAV -> syncWebDav(context, backend)
            Backend.Type.GDRIVE -> syncGDrive(context, backend)
            else -> Unit
        }

        backend.lastSyncDate = Date()
        backend.save()
    }
}