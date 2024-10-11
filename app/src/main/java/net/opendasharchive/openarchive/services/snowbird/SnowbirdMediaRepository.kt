package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.features.main.ApiResponse

interface ISnowbirdMediaRepository {
    suspend fun fetchMedia(repoId: String, forceRefresh: Boolean = false): SnowbirdResult<List<Media>>
}

class SnowbirdMediaRepository(val api: ISnowbirdAPI) : ISnowbirdMediaRepository {
    override suspend fun fetchMedia(repoId: String, forceRefresh: Boolean): SnowbirdResult<List<Media>> {
        return if (forceRefresh) {
            fetchFromNetwork(repoId)
        } else {
            fetchFromCache()
        }
    }

    private suspend fun fetchFromNetwork(repoId: String): SnowbirdResult<List<Media>> {
        return when (val response = api.fetchFiles(repoId)) {
            is ApiResponse.ListResponse -> {
                SnowbirdResult.Success(emptyList()) // TODO: SnowbirdResult.Success(response.data)
            }
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    private fun fetchFromCache(): SnowbirdResult<List<Media>> {
        return SnowbirdResult.Success(Media.getAll())
    }
}