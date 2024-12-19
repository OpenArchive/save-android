package net.opendasharchive.openarchive.db

interface IFolderDataSource {
    suspend fun archiveFolder(folderId: Long): Result<Unit>
    suspend fun deleteFolder(folderId: Long): Boolean
    suspend fun getFolders(): List<Folder>
    suspend fun getFolderCount(): Result<Int>
    suspend fun saveFolder(folder: Folder)
    suspend fun getFolder(id: Long): Result<Folder?>
    suspend fun getFoldersByBackend(backend: Backend): List<Folder>
}