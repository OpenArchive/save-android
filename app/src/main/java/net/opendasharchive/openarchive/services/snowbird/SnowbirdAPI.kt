package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.features.main.HttpMethod
import net.opendasharchive.openarchive.features.main.UnixSocketClient

class SnowbirdAPI(private var client: UnixSocketClient) {

    suspend fun getGroups(): ApiResponse<SnowbirdGroup> {
        return client.sendRequest<SnowbirdGroup>("/api/groups")
    }

    suspend fun createGroup(): ApiResponse<SnowbirdGroup> {
        return client.sendRequest<SnowbirdGroup>("/api/groups", HttpMethod.POST)
    }
}