package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.features.main.UnixSocketClient

class SnowbirdAPI(private var client: UnixSocketClient) {

    suspend fun getGroups(): ApiResponse<List<SnowbirdGroup>> {
        return client.sendRequest<SnowbirdGroup>("/api/groups")
    }

//    suspend fun addGroup(group: SnowbirdGroup): ApiResponse<String> {
//        return client.sendRequest<String>("/api/groups", "POST", group)
//    }
}