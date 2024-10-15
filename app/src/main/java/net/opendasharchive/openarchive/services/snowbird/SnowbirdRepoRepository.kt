package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.db.SnowbirdRepo

interface ISnowbirdRepoRepository {
    suspend fun createRepo(groupKey: String, repoName: String): SnowbirdResult<SnowbirdRepo>
    suspend fun fetchRepos(groupKey: String, forceRefresh: Boolean = false): SnowbirdResult<List<SnowbirdRepo>>
}

class SnowbirdRepoRepository(val api: ISnowbirdAPI) : ISnowbirdRepoRepository {
    override suspend fun createRepo(groupKey: String, repoName: String): SnowbirdResult<SnowbirdRepo> {
        return when (val response = api.createRepo(groupKey, repoName)) {
            is ApiResponse.SingleResponse -> SnowbirdResult.Success(response.data)
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    override suspend fun fetchRepos(groupKey: String, forceRefresh: Boolean): SnowbirdResult<List<SnowbirdRepo>> {
        return if (forceRefresh) {
            fetchFromNetwork(groupKey)
        } else {
            fetchFromCache(groupKey)
        }
    }

    private suspend fun fetchFromNetwork(groupKey: String): SnowbirdResult<List<SnowbirdRepo>> {
        return when (val response = api.fetchRepos(groupKey)) {
            is ApiResponse.ListResponse -> {
                SnowbirdResult.Success(response.data)
            }
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    private fun fetchFromCache(groupKey: String): SnowbirdResult<List<SnowbirdRepo>> {
        return SnowbirdResult.Success(SnowbirdRepo.getAllFor(SnowbirdGroup.get(groupKey)))
    }
}