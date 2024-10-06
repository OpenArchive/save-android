package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.features.main.HttpMethod
import net.opendasharchive.openarchive.features.main.UnixSocketClient

class SnowbirdAPI(private var client: UnixSocketClient) {

    companion object {
        private const val BASE_PATH = "/api"
        private const val GROUPS_PATH = "$BASE_PATH/groups"
    }

    suspend fun getGroups(): ApiResponse<SnowbirdGroup> {
        return client.sendRequest<SnowbirdGroup>(GROUPS_PATH, HttpMethod.GET)
    }

    suspend fun getGroup(groupId: String): ApiResponse<SnowbirdGroup> {
        return client.sendRequest<SnowbirdGroup>("$GROUPS_PATH/$groupId", HttpMethod.GET)
    }

    suspend fun createGroup(group: SnowbirdGroup): ApiResponse<SnowbirdGroup> {
        return client.sendRequest<SnowbirdGroup>(GROUPS_PATH, HttpMethod.POST, group)
    }
}