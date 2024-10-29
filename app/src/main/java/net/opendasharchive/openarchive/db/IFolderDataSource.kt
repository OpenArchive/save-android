package net.opendasharchive.openarchive.db

interface IFolderDataSource {
    suspend fun archiveFolder(folderId: Long)
    suspend fun deleteFolder(folderId: Long): Boolean
    suspend fun getFolders(): List<Folder>
    suspend fun getFolderCount(): Int
    suspend fun saveFolder(folder: Folder)
    suspend fun getFolder(id: Long): Folder?
    suspend fun getFoldersByBackend(backend: Backend): List<Folder>
}