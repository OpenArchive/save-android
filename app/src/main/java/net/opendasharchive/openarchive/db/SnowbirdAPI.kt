package net.opendasharchive.openarchive.db

import kotlinx.serialization.Serializable
import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.features.main.ClientResponse
import net.opendasharchive.openarchive.features.main.HttpMethod
import net.opendasharchive.openarchive.features.main.UnixSocketClient

@Serializable
sealed interface SerializableMarker

@Serializable
data class RequestName(val name: String): SerializableMarker

class SnowbirdAPI(private var client: UnixSocketClient) {

    companion object {
        private const val BASE_PATH = "/api"
        private const val GROUPS_PATH = "$BASE_PATH/groups"
        private const val REPOS_PATH = "$BASE_PATH/groups/%s/repos"
    }

    suspend fun fetchGroups(): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<SnowbirdGroupList>(GROUPS_PATH, HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.ListResponse(response.data.groups)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    suspend fun fetchGroup(key: String): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<SnowbirdGroup>("$GROUPS_PATH/$key", HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    suspend fun createGroup(groupName: String): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<RequestName, SnowbirdGroup>(GROUPS_PATH, HttpMethod.POST, RequestName(groupName))) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    suspend fun fetchRepos(groupId: String): ApiResponse<SnowbirdRepo> {
        return when (val response = client.sendRequest<SnowbirdRepoList>(REPOS_PATH.format(groupId), HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.ListResponse(response.data.repos)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }
}