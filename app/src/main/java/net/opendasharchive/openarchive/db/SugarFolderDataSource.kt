package net.opendasharchive.openarchive.db

import com.orm.query.Select
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SugarFolderDataSource : IFolderDataSource {
    override suspend fun archiveFolder(folderId: Long) {
        getFolder(folderId)?.let { folder ->
            folder.archived = true
            folder.save()
        }
    }

    override suspend fun deleteFolder(folderId: Long): Boolean {
        return true
    }

    override suspend fun getFolders(): List<Folder> = withContext(Dispatchers.IO) {
        Select.from(Folder::class.java).list()
    }

    override suspend fun getFolderCount(): Int = withContext(Dispatchers.IO) {
        Select.from(Folder::class.java).count().toLong().toInt()
    }

    override suspend fun getFolder(id: Long): Folder? = withContext(Dispatchers.IO) {
        Select.from(Folder::class.java)
            .where("id = ?", arrayOf(id.toString()))
            .first()
    }

    override suspend fun getFoldersByBackend(backend: Backend): List<Folder> =
        withContext(Dispatchers.IO) {
            Select.from(Folder::class.java)
                .where("backend = ?", arrayOf(backend.id.toString())).list()
        }

    override suspend fun saveFolder(folder: Folder) {
        folder.save()
    }
}