package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.SnowbirdAPI
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.features.main.ApiResponse

interface ISnowbirdRepoRepository {
    suspend fun fetchRepos(groupId: String, forceRefresh: Boolean = false): SnowbirdResult<List<SnowbirdRepo>>
}

class SnowbirdRepoRepository(val api: SnowbirdAPI) : ISnowbirdRepoRepository {
    override suspend fun fetchRepos(groupId: String, forceRefresh: Boolean): SnowbirdResult<List<SnowbirdRepo>> {
        return if (forceRefresh) {
            when (val response = api.fetchRepos(groupId)) {
                is ApiResponse.ListResponse -> SnowbirdResult.Success(response.data)
                is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
                else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
            }
        } else {
            SnowbirdResult.Success(SnowbirdRepo.getAll())
        }
    }
}