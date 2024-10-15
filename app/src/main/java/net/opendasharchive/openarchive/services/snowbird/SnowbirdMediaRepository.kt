package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.db.SnowbirdError

typealias MediaID = String

interface ISnowbirdMediaRepository {
    suspend fun downloadMedia(groupKey: String, repoKey: String, mediaKey: String): SnowbirdResult<Media>
    suspend fun uploadMedia(groupKey: String, repoKey: String, media: Media): SnowbirdResult<MediaID>
}

class SnowbirdMediaRepository(val api: ISnowbirdAPI) : ISnowbirdMediaRepository {
    override suspend fun downloadMedia(groupKey: String, repoKey: String, mediaKey: String): SnowbirdResult<Media> {
        return when (val response = api.downloadMedia(groupKey, repoKey, mediaKey)) {
            is ApiResponse.ListResponse -> SnowbirdResult.Success(Media())
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

    override suspend fun uploadMedia(groupKey: String, repoKey: String, media: Media): SnowbirdResult<MediaID> {
        return when (val response = api.uploadMedia(groupKey, repoKey, media)) {
            is ApiResponse.ListResponse -> SnowbirdResult.Success("UUID")
            is ApiResponse.ErrorResponse -> SnowbirdResult.Failure(SnowbirdError.GeneralError(response.error.friendlyMessage))
            else -> SnowbirdResult.Failure(SnowbirdError.GeneralError("Unexpected response type"))
        }
    }

}