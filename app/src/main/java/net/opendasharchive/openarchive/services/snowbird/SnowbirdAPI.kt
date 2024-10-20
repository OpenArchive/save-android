package net.opendasharchive.openarchive.services.snowbird

import android.content.Context
import android.net.Uri
import net.opendasharchive.openarchive.db.ApiError
import net.opendasharchive.openarchive.db.EmptyRequest
import net.opendasharchive.openarchive.db.FileUploadResult
import net.opendasharchive.openarchive.db.JoinGroupResponse
import net.opendasharchive.openarchive.db.MembershipRequest
import net.opendasharchive.openarchive.db.RequestName
import net.opendasharchive.openarchive.db.SnowbirdFileItem
import net.opendasharchive.openarchive.db.SnowbirdFileList
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.db.SnowbirdGroupList
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.db.SnowbirdRepoList
import net.opendasharchive.openarchive.extensions.createInputStream
import net.opendasharchive.openarchive.extensions.getFilename
import net.opendasharchive.openarchive.features.main.ClientResponse
import net.opendasharchive.openarchive.features.main.HttpMethod
import net.opendasharchive.openarchive.features.main.UnixSocketClient
import net.opendasharchive.openarchive.features.main.downloadFile
import net.opendasharchive.openarchive.features.main.uploadFile

interface ISnowbirdAPI {
    // Media
    suspend fun fetchFiles(groupKey: String, repoKey: String): ApiResponse<SnowbirdFileItem>
    suspend fun downloadFile(groupKey: String, repoKey: String, filename: String): ApiResponse<ByteArray>
    suspend fun uploadFile(groupKey: String, repoKey: String, uri: Uri): ApiResponse<FileUploadResult>

    // Groups
    suspend fun createGroup(groupName: String): ApiResponse<SnowbirdGroup>
    suspend fun fetchGroup(key: String): ApiResponse<SnowbirdGroup>
    suspend fun fetchGroups(): ApiResponse<SnowbirdGroup>
    suspend fun joinGroup(uriString: String): ApiResponse<JoinGroupResponse>

    // Repos
    suspend fun createRepo(groupKey: String, repoName: String): ApiResponse<SnowbirdRepo>
    suspend fun fetchRepos(groupKey: String): ApiResponse<SnowbirdRepo>
}
class SnowbirdAPI(private var context: Context, private var client: UnixSocketClient): ISnowbirdAPI {

    companion object {
        private const val BASE_PATH = "/api"
        const val MEMBERSHIPS_PATH = "$BASE_PATH/memberships"
        const val GROUPS_PATH = "$BASE_PATH/groups"
        const val REPOS_PATH = "$BASE_PATH/groups/%s/repos"
        const val MEDIA_PATH = "$BASE_PATH/groups/%s/repos/%s/media"
        const val MEDIA_PATH_UPLOAD = "$BASE_PATH/groups/%s/repos/%s/media/%s"
    }

    override suspend fun downloadFile(groupKey: String, repoKey: String, filename: String): ApiResponse<ByteArray> {
        return when (val response = client.downloadFile(
            MEDIA_PATH_UPLOAD.format(groupKey, repoKey, filename))
        ) {
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(response.data)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun fetchFiles(groupKey: String, repoKey: String): ApiResponse<SnowbirdFileItem> {
        return when (val response = client.sendRequest<EmptyRequest, SnowbirdFileList>(
            endpoint = MEDIA_PATH.format(groupKey, repoKey), HttpMethod.GET)
        ) {
            is ClientResponse.SuccessResponse -> ApiResponse.ListResponse(response.data.files)
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(response.error)
        }
    }

    override suspend fun uploadFile(groupKey: String, repoKey: String, uri: Uri): ApiResponse<FileUploadResult> {
        val inputStream = uri.createInputStream(context) ?: return ApiResponse.ErrorResponse(ApiError.ResourceNotFound)
        val filename = uri.getFilename(context) ?: return ApiResponse.ErrorResponse(ApiError.ResourceNotFound)

        return when (val response = client.uploadFile<FileUploadResult>(
            endpoint = MEDIA_PATH_UPLOAD.format(groupKey, repoKey, filename),
            inputStream = inputStream)
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

    override suspend fun joinGroup(uriString: String): ApiResponse<JoinGroupResponse> {
        return when (val response = client.sendRequest<MembershipRequest, JoinGroupResponse>(MEMBERSHIPS_PATH, HttpMethod.POST, MembershipRequest(uriString))) {
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