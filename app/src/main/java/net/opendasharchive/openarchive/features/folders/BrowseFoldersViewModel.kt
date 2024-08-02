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
import net.opendasharchive.openarchive.services.SaveClient
import net.opendasharchive.openarchive.services.gdrive.GDriveConduit
import timber.log.Timber
import java.io.IOException
import java.util.Date

class BrowseFoldersViewModel : ViewModel() {

    data class Folder(val name: String, val modified: Date)

    private val mFolders = MutableLiveData<List<Folder>>()

    val folders: LiveData<List<Folder>>
        get() = mFolders

    val progressBarFlag = MutableLiveData(false)

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

                mFolders.value = value.filter { !backend.hasProject(it.name) }

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
                Folder(it.name, it.modified ?: Date())
            }
            else {
                null
            }
        } ?: emptyList()
    }

    private fun getGDriveFolders(context: Context, backend: Backend): List<Folder> {
        return GDriveConduit.listFoldersInRoot(GDriveConduit.getDrive(context))
    }
}