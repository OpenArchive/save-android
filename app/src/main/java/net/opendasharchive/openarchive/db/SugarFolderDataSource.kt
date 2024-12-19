package net.opendasharchive.openarchive.db

import com.orm.query.Condition
import com.orm.query.Select
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SugarFolderDataSource(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IFolderDataSource {
    override suspend fun archiveFolder(folderId: Long): Result<Unit> =
        withContext(dispatcher) {
            runCatching {
                val folder = getFolder(folderId).getOrThrow()
                    ?: throw IllegalStateException("No folder found with id: $folderId")
                folder.archived = true
                folder.save()
                Unit
            }
        }

    override suspend fun deleteFolder(folderId: Long): Boolean {
        return true
    }

    override suspend fun getFolders(): List<Folder> = withContext(dispatcher) {
        Select.from(Folder::class.java).list()
    }

    override suspend fun getFolderCount(): Result<Int> = withContext(dispatcher) {
        runCatching {
            Select.from(Folder::class.java).count().toInt()
        }
    }

    override suspend fun getFolder(id: Long): Result<Folder?> =
        withContext(dispatcher) {
            runCatching {
                Select.from(Folder::class.java)
                    .where(Condition.prop("id").eq(id))
                    .first()
            }
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