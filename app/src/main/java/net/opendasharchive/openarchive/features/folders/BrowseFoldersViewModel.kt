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
import java.io.IOException
import java.util.Date

class BrowseFoldersViewModel : ViewModel() {

    // data class Folder(var name: String, var backend: Backend, val modified: Date)

    private val mFolders = MutableLiveData<List<Folder>>()

    val folders: LiveData<List<Folder>>
        get() = mFolders

    val progressBarFlag = MutableLiveData(false)

    fun getAllFolders(context: Context) {
        viewModelScope.launch {
            progressBarFlag.value = true

//            try {
            val value = withContext(Dispatchers.IO) {
                var allFolders = mutableListOf<Folder>()

                for (backend in Backend.getAll()) {
                    Timber.d("Getting folders for ${backend.friendlyName}")

                    val someFolders = when (backend.tType) {
                        Backend.Type.WEBDAV -> getWebDavFolders(context, backend)
                        Backend.Type.GDRIVE -> getGDriveFolders(context, backend)
                        else -> emptyList()
                    }

                    allFolders.addAll(someFolders)
                }

                allFolders
            }

            progressBarFlag.value = false
            mFolders.value = value

//            }
//            catch (e: Error) {
//                mFolders.value = arrayListOf()
//                Timber.e(e)
//            } finally {
//                progressBarFlag.value = false
//            }
        }
    }

    fun getFolders(context: Context, backend: Backend) {
        viewModelScope.launch {
            progressBarFlag.value = true

            try {
                val value = withContext(Dispatchers.IO) {
                    when (backend.tType) {
                        Backend.Type.WEBDAV -> getWebDavFolders(context, backend)
                        Backend.Type.GDRIVE -> getGDriveFolders(context, backend)
                        else -> emptyList()
                    }
                }

                mFolders.value = value.filter { !backend.hasFolder(it.description) }

                progressBarFlag.value = false
            }
            catch (e: Throwable) {
                progressBarFlag.value = false
                mFolders.value = arrayListOf()
                Timber.e(e)
            }
        }
    }

    @Throws(IOException::class)
    private suspend fun getWebDavFolders(context: Context, backend: Backend): List<Folder> {
        val root = backend.hostUrl?.encodedPath

        return SaveClient.getSardine(context, backend).list(backend.host)?.mapNotNull {
            if (it?.isDirectory == true && it.path != root) {
                Folder(
                    description = it.name,
                    backendId = backend.id,
                    created = it.modified ?: Date())
            }
            else {
                null
            }
        } ?: emptyList()
    }

    private fun getGDriveFolders(context: Context, backend: Backend): List<Folder> {
        return GDriveConduit.listFoldersInRoot(GDriveConduit.getDrive(context), backend)
    }
}