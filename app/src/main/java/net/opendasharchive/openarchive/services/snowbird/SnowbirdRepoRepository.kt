package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.features.main.ApiResponse

interface ISnowbirdRepoRepository {
    suspend fun fetchRepos(groupId: String, forceRefresh: Boolean = false): SnowbirdResult<List<SnowbirdRepo>>
}

class SnowbirdRepoRepository(val api: ISnowbirdAPI) : ISnowbirdRepoRepository {
    override suspend fun fetchRepos(groupId: String, forceRefresh: Boolean): SnowbirdResult<List<SnowbirdRepo>> {
        return if (forceRefresh) {
            fetchFromNetwork(groupId)
        } else {
            fetchFromCache()
        }
    }

    private suspend fun fetchFromNetwork(groupId: String): SnowbirdResult<List<SnowbirdRepo>> {
        return when (val response = api.fetchRepos(groupId)) {
            is ApiResponse.ListResponse -> {
                saveRepos(response.data)
                SnowbirdResult.Success(response.data)
            }
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    private fun fetchFromCache(): SnowbirdResult<List<SnowbirdRepo>> {
        return SnowbirdResult.Success(SnowbirdRepo.getAll())
    }

    private fun saveRepo(repo: SnowbirdRepo) {
        repo.save()
    }

    private fun saveRepos(repos: List<SnowbirdRepo>) {
        repos.forEach { repo ->
            repo.save()
        }
    }

}