package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.SnowbirdAPI
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.features.main.ApiResponse

interface ISnowbirdGroupRepository {
    suspend fun createGroup(groupName: String): SnowbirdResult<SnowbirdGroup>
    suspend fun fetchGroup(groupId: String): SnowbirdResult<SnowbirdGroup>
    suspend fun fetchGroups(forceRefresh: Boolean = false): SnowbirdResult<List<SnowbirdGroup>>
}

class SnowbirdGroupRepository(val api: SnowbirdAPI) : ISnowbirdGroupRepository {
    private var lastFetchTime: Long = 0
    private val cacheValidityPeriod: Long = 5 * 60 * 1000

    override suspend fun createGroup(groupName: String): SnowbirdResult<SnowbirdGroup> {
        return when (val response = api.createGroup(groupName)) {
            is ApiResponse.SingleResponse -> {
                saveGroup(response.data)
                SnowbirdResult.Success(response.data)
            }
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    override suspend fun fetchGroup(groupId: String): SnowbirdResult<SnowbirdGroup> {
        return when (val response = api.fetchGroups()) {
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

    private suspend fun fetchFromNetwork(): SnowbirdResult<List<SnowbirdGroup>> {
        return when (val response = api.fetchGroups()) {
            is ApiResponse.ListResponse -> {
                saveGroups(response.data)
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

    private fun saveGroup(group: SnowbirdGroup) {
        group.save()
    }

    private fun saveGroups(groups: List<SnowbirdGroup>) {
        groups.forEach { group ->
            group.save()
        }
    }
}