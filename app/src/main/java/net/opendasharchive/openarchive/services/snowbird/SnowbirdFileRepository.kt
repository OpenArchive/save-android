package net.opendasharchive.openarchive.services.snowbird

import android.net.Uri
import net.opendasharchive.openarchive.db.FileUploadResult
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdFileItem

interface ISnowbirdFileRepository {
    suspend fun fetchFiles(groupKey: String, repoKey: String, forceRefresh: Boolean = false): SnowbirdResult<List<SnowbirdFileItem>>
    suspend fun downloadFile(groupKey: String, repoKey: String, filename: String): SnowbirdResult<ByteArray>
    suspend fun uploadFile(groupKey: String, repoKey: String, uri: Uri): SnowbirdResult<FileUploadResult>
}

class SnowbirdFileRepository(val api: ISnowbirdAPI) : ISnowbirdFileRepository {
    override suspend fun fetchFiles(groupKey: String, repoKey: String, forceRefresh: Boolean): SnowbirdResult<List<SnowbirdFileItem>> {
        return if (forceRefresh) {
            fetchFilesFromNetwork(groupKey, repoKey)
        } else {
            fetchFilesFromCache(groupKey, repoKey)
        }
    }

    private fun fetchFilesFromCache(groupKey: String, repoKey: String): SnowbirdResult<List<SnowbirdFileItem>> {
        return SnowbirdResult.Success(SnowbirdFileItem.findBy(groupKey, repoKey))
    }

    private suspend fun fetchFilesFromNetwork(groupKey: String, repoKey: String): SnowbirdResult<List<SnowbirdFileItem>> {
        return when (val response = api.fetchFiles(groupKey, repoKey)) {
            is ApiResponse.ListResponse -> SnowbirdResult.Success(response.data)
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    override suspend fun downloadFile(groupKey: String, repoKey: String, filename: String): SnowbirdResult<ByteArray> {
        return when (val response = api.downloadFile(groupKey, repoKey, filename)) {
            is ApiResponse.SingleResponse -> SnowbirdResult.Success(response.data)
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    override suspend fun uploadFile(groupKey: String, repoKey: String, uri: Uri): SnowbirdResult<FileUploadResult> {
        return when (val response = api.uploadFile(groupKey, repoKey, uri)) {
            is ApiResponse.SingleResponse -> SnowbirdResult.Success(response.data)
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

}