package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.RequestName
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.db.SnowbirdGroupList
import net.opendasharchive.openarchive.db.SnowbirdMediaDTO
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.db.SnowbirdRepoList
import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.features.main.ClientResponse
import net.opendasharchive.openarchive.features.main.HttpMethod
import net.opendasharchive.openarchive.features.main.UnixSocketClient

interface ISnowbirdAPI {
    // Files
    suspend fun fetchFiles(repoId: String): ApiResponse<SnowbirdMediaDTO>

    // Groups
    suspend fun createGroup(groupName: String, repoName: String): ApiResponse<SnowbirdGroup>
    suspend fun fetchGroup(key: String): ApiResponse<SnowbirdGroup>
    suspend fun fetchGroups(): ApiResponse<SnowbirdGroup>
    suspend fun joinGroup(uriString: String): ApiResponse<SnowbirdGroup>

    // Repos
    suspend fun fetchRepos(groupId: String): ApiResponse<SnowbirdRepo>
}
class SnowbirdAPI(private var client: UnixSocketClient): ISnowbirdAPI {

    companion object {
        private const val BASE_PATH = "/api"
        private const val GROUPS_PATH = "$BASE_PATH/groups"
        private const val REPOS_PATH = "$BASE_PATH/groups/%s/repos"
        private const val MEDIA_PATH = "$BASE_PATH/groups/%s/repos/%s/media"
    }

    override suspend fun fetchFiles(repoId: String): ApiResponse<SnowbirdMediaDTO> {
        return when (val response = client.sendRequest<SnowbirdMediaDTO>(GROUPS_PATH, HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun createGroup(groupName: String, repoName: String): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<RequestName, SnowbirdGroup>(GROUPS_PATH, HttpMethod.POST, RequestName(groupName))) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun fetchGroup(key: String): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<SnowbirdGroup>("$GROUPS_PATH/$key", HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun fetchGroups(): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<SnowbirdGroupList>(GROUPS_PATH, HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.ListResponse(response.data.groups)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    // TODO: This is not functional yet.
    override suspend fun joinGroup(uriString: String): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<SnowbirdGroup>("$GROUPS_PATH/$uriString", HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun fetchRepos(groupId: String): ApiResponse<SnowbirdRepo> {
        return when (val response = client.sendRequest<SnowbirdRepoList>(REPOS_PATH.format(groupId), HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.ListResponse(response.data.repos)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }
}