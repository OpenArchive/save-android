package net.opendasharchive.openarchive.db

interface IFolderRepository {
    suspend fun getCurrentFolder(): Folder?
}

class FolderRepository : IFolderRepository {
    override suspend fun getCurrentFolder(): Folder? {
        return Folder.current
    }
}