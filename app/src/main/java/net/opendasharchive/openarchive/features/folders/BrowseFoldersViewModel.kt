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

    private val mItems = MutableLiveData<List<ListItem>>()

    val items: LiveData<List<ListItem>>
        get() = mItems

    val progressBarFlag = MutableLiveData(false)

    fun loadData(context: Context) {
        viewModelScope.launch {
            progressBarFlag.value = true

            withContext(Dispatchers.IO) {
                for (backend in Backend.getAll()) {
                    Timber.d("Syncing folders for ${backend.friendlyName}")

                    when (backend.tType) {
                        Backend.Type.WEBDAV -> syncWebDav(context, backend)
                        Backend.Type.GDRIVE -> syncGDrive(context, backend)
                        else -> Unit
                    }
                }
            }

            try {
                val value = withContext(Dispatchers.IO) {
                    val allFolders = mutableSetOf<ListItem>()

                    for (backend in Backend.getAll()) {
                        Timber.d("Getting folders for ${backend.friendlyName}")

                        val someFolders = getLocalFolders(backend)

                        allFolders.addAll(someFolders)
                    }

                    allFolders.toList()
                }

                mItems.value = value
            }
            catch (e: Error) {
                mItems.value = emptyList()
                Timber.e(e)
                throw(e)
            } finally {
                progressBarFlag.value = false
            }
        }
    }

    private fun getLocalFolders(backend: Backend): List<ListItem> {
        val folders = Folder.getLocalFoldersForBackend(backend).map { ListItem.ContentItem(it) }

        if (folders.isNotEmpty()) {
            val allItems = mutableListOf<ListItem>()
            allItems.add(ListItem.SectionHeader(backend.name))
            allItems.addAll(folders)
            return allItems
        }

        return emptyList()
    }

//    @Throws(IOException::class)
//    private suspend fun getWebDavFolders(context: Context, backend: Backend): List<ListItem> {
//        val root = backend.hostUrl?.encodedPath
//
//        val folders = SaveClient.getSardine(context, backend).list(backend.host)?.mapNotNull {
//            if (it?.isDirectory == true && it.path != root) {
//                ListItem.ContentItem(Folder(
//                    description = it.name,
//                    backend = backend,
//                    created = it.modified ?: Date()))
//            }
//            else {
//                null
//            }
//        } ?: emptyList()
//
//        if (folders.isNotEmpty()) {
//            val allItems = mutableListOf<ListItem>()
//            allItems.add(ListItem.SectionHeader("Private Server"))
//            allItems.addAll(folders)
//            return allItems
//        }
//
//        return emptyList()
//    }
//
//    private fun getGDriveFolders(context: Context, backend: Backend): List<ListItem> {
//        val folders = GDriveConduit.listFoldersInRoot(GDriveConduit.getDrive(context), backend).map { ListItem.ContentItem(it) }
//
//        if (folders.isNotEmpty()) {
//            val allItems = mutableListOf<ListItem>()
//            allItems.add(ListItem.SectionHeader("Google Drive"))
//            allItems.addAll(folders)
//            return allItems
//        }
//
//        return emptyList()
//    }

    private fun syncGDrive(context: Context, backend: Backend) {
        GDriveConduit.listFoldersInRoot(GDriveConduit.getDrive(context), backend).forEach { folder ->
            if (folder.doesNotExist()) {
                Timber.d("Syncing ${folder.description}")
                folder.save()
            }
        }
    }

    private suspend fun syncWebDav(context: Context, backend: Backend) {
        val root = backend.hostUrl?.encodedPath

        SaveClient.getSardine(context, backend).list(backend.host)?.mapNotNull {
            if (it?.isDirectory == true && it.path != root) {
                val folder = Folder(
                    description = it.name,
                    backend = backend,
                    created = it.modified ?: Date()
                )

                if (folder.doesNotExist()) {
                    Timber.d("Syncing ${folder.description}")
                    folder.save()
                }
            }
        }
    }
}