package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.EmptyRequest
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.db.MembershipRequest
import net.opendasharchive.openarchive.db.RequestName
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.db.SnowbirdGroupList
import net.opendasharchive.openarchive.db.SnowbirdMediaItem
import net.opendasharchive.openarchive.db.SnowbirdMediaList
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.db.SnowbirdRepoList
import net.opendasharchive.openarchive.features.main.ClientResponse
import net.opendasharchive.openarchive.features.main.HttpMethod
import net.opendasharchive.openarchive.features.main.UnixSocketClient

interface ISnowbirdAPI {
    // Media
    suspend fun fetchMedia(groupKey: String, repoKey: String): ApiResponse<SnowbirdMediaItem>
    suspend fun downloadMedia(groupKey: String, repoKey: String, mediaKey: String): ApiResponse<SnowbirdMediaItem>
    suspend fun uploadMedia(groupKey: String, repoKey: String, media: Media): ApiResponse<SnowbirdMediaItem>

    // Groups
    suspend fun createGroup(groupName: String): ApiResponse<SnowbirdGroup>
    suspend fun fetchGroup(key: String): ApiResponse<SnowbirdGroup>
    suspend fun fetchGroups(): ApiResponse<SnowbirdGroup>
    suspend fun joinGroup(uriString: String): ApiResponse<SnowbirdGroup>

    // Repos
    suspend fun createRepo(groupKey: String, repoName: String): ApiResponse<SnowbirdRepo>
    suspend fun fetchRepos(groupKey: String): ApiResponse<SnowbirdRepo>
}
class SnowbirdAPI(private var client: UnixSocketClient): ISnowbirdAPI {

    companion object {
        private const val BASE_PATH = "/api"
        const val MEMBERSHIPS_PATH = "$BASE_PATH/memberships"
        const val GROUPS_PATH = "$BASE_PATH/groups"
        const val REPOS_PATH = "$BASE_PATH/groups/%s/repos"
        const val MEDIA_PATH = "$BASE_PATH/groups/%s/repos/%s/media"
        const val MEDIA_PATH_UPLOAD = "$BASE_PATH/groups/%s/repos/%s/media/%s"
    }

    override suspend fun downloadMedia(groupKey: String, repoKey: String, mediaKey: String): ApiResponse<SnowbirdMediaItem> {
        return when (val response = client.sendRequest<EmptyRequest, SnowbirdMediaItem>(
            MEDIA_PATH_UPLOAD.format(groupKey, repoKey, mediaKey), HttpMethod.GET)
        ) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun fetchMedia(groupKey: String, repoKey: String): ApiResponse<SnowbirdMediaItem> {
        return when (val response = client.sendRequest<RequestName, SnowbirdMediaList>(
            endpoint = MEDIA_PATH.format(groupKey, repoKey), HttpMethod.GET)
        ) {
            is ClientResponse.SuccessResponse -> ApiResponse.ListResponse(response.data.media)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun uploadMedia(groupKey: String, repoKey: String, media: Media): ApiResponse<SnowbirdMediaItem> {
        return when (val response = client.sendBinaryRequest<SnowbirdMediaItem>(
            endpoint = MEDIA_PATH.format(groupKey, repoKey), HttpMethod.GET, media.file.inputStream())
        ) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun createGroup(groupName: String): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<RequestName, SnowbirdGroup>(GROUPS_PATH, HttpMethod.POST, RequestName(groupName))) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun fetchGroup(key: String): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<EmptyRequest, SnowbirdGroup>("$GROUPS_PATH/$key", HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun fetchGroups(): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<EmptyRequest, SnowbirdGroupList>(GROUPS_PATH, HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.ListResponse(response.data.groups)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun joinGroup(uriString: String): ApiResponse<SnowbirdGroup> {
        return when (val response = client.sendRequest<MembershipRequest, SnowbirdGroup>(MEMBERSHIPS_PATH, HttpMethod.POST, MembershipRequest(uriString))) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun createRepo(groupKey: String, repoName: String): ApiResponse<SnowbirdRepo> {
        return when (val response = client.sendRequest<RequestName, SnowbirdRepo>(REPOS_PATH.format(groupKey), HttpMethod.POST, RequestName(repoName))) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun fetchRepos(groupKey: String): ApiResponse<SnowbirdRepo> {
        return when (val response = client.sendRequest<EmptyRequest, SnowbirdRepoList>(REPOS_PATH.format(groupKey), HttpMethod.GET)) {
            is ClientResponse.SuccessResponse -> ApiResponse.ListResponse(response.data.repos)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }
}