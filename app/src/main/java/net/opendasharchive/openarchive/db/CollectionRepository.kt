package net.opendasharchive.openarchive.db

interface ICollectionRepository {
    suspend fun getCollectionsByFolder(folderId: Long): List<Collection>
}

class CollectionRepository : ICollectionRepository {
    override suspend fun getCollectionsByFolder(folderId: Long): List<Collection> {
        return Collection.getByFolder(folderId)
    }
}