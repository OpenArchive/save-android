package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.db.SnowbirdRepo
import timber.log.Timber

interface ISnowbirdRepoRepository {
    suspend fun createRepo(groupKey: String, repoName: String): SnowbirdResult<SnowbirdRepo>
    suspend fun fetchRepos(groupKey: String, forceRefresh: Boolean = false): SnowbirdResult<List<SnowbirdRepo>>
}

class SnowbirdRepoRepository(val api: ISnowbirdAPI) : ISnowbirdRepoRepository {
    override suspend fun createRepo(groupKey: String, repoName: String): SnowbirdResult<SnowbirdRepo> {
        Timber.d("Creating repo: groupKey=$groupKey, repoName=$repoName")
        return try {
            val response = api.createRepo(groupKey, repoName)
            Timber.d("Received response from API: $response")
            when (response) {
                is ApiResponse.SingleResponse -> {
                    Timber.d("Repo created successfully: ${response.data}")
                    SnowbirdResult.Success(response.data)
                }
                is ApiResponse.ErrorResponse -> {
                    Timber.e("Error creating repo: ${response.error.friendlyMessage}")
                    SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
                }
                else -> {
                    Timber.e("Unexpected response type: ${response::class.simpleName}")
                    SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
                }
            }
        } catch (e: Exception) {
            Timber.e("Exception while creating repo", e)
            SnowbirdResult.Failure(SnowbirdError.GeneralError(e.message ?: "Unknown error"))
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