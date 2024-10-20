package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.JoinGroupResponse
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdGroup

interface ISnowbirdGroupRepository {
    suspend fun createGroup(groupName: String): SnowbirdResult<SnowbirdGroup>
    suspend fun fetchGroup(groupKey: String): SnowbirdResult<SnowbirdGroup>
    suspend fun fetchGroups(forceRefresh: Boolean = false): SnowbirdResult<List<SnowbirdGroup>>
    suspend fun joinGroup(uriString: String): SnowbirdResult<JoinGroupResponse>
}

class SnowbirdGroupRepository(val api: ISnowbirdAPI) : ISnowbirdGroupRepository {
    private var lastFetchTime: Long = 0
    private val cacheValidityPeriod: Long = 5 * 60 * 1000

    override suspend fun createGroup(groupName: String): SnowbirdResult<SnowbirdGroup> {
        return when (val response = api.createGroup(groupName)) {
            is ApiResponse.SingleResponse -> SnowbirdResult.Success(response.data)
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    override suspend fun fetchGroup(groupKey: String): SnowbirdResult<SnowbirdGroup> {
        return when (val response = api.fetchGroup(groupKey)) {
            is ApiResponse.SingleResponse -> SnowbirdResult.Success(response.data)
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    override suspend fun fetchGroups(forceRefresh: Boolean): SnowbirdResult<List<SnowbirdGroup>> {
        val currentTime = System.currentTimeMillis()
        val shouldFetchFromNetwork = forceRefresh || currentTime - lastFetchTime > cacheValidityPeriod

        return if (forceRefresh) {
            fetchFromNetwork()
        } else {
            fetchFromCache()
        }
    }

    override suspend fun joinGroup(uriString: String): SnowbirdResult<JoinGroupResponse> {
        return when (val response = api.joinGroup(uriString)) {
            is ApiResponse.SingleResponse -> SnowbirdResult.Success(response.data)
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    private suspend fun fetchFromNetwork(): SnowbirdResult<List<SnowbirdGroup>> {
        return when (val response = api.fetchGroups()) {
            is ApiResponse.ListResponse -> {
                lastFetchTime = System.currentTimeMillis()
                SnowbirdResult.Success(response.data)
            }
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    private suspend fun fetchFromCache(): SnowbirdResult<List<SnowbirdGroup>> {
        return SnowbirdResult.Success(SnowbirdGroup.getAll())
//        val cachedGroups = SnowbirdGroup.getAll()
//        return if (cachedGroups.isNotEmpty()) {
//            SnowbirdResult.Success(cachedGroups)
//        } else {
//            fetchFromNetwork()
//        }
    }
}