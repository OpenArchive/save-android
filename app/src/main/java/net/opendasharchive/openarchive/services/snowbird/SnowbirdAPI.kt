package net.opendasharchive.openarchive.services.snowbird

import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.features.main.UnixSocketClient

class SnowbirdAPI(private var client: UnixSocketClient) {

    suspend fun getGroups(): ApiResponse<List<SnowbirdGroup>> {
        return client.sendRequest<List<SnowbirdGroup>>("/api/groups")

//        when (val response = client.sendRequest<List<String>>("/api/groups")) {
//            is ApiResponse.Success -> {
//                val data = response.data
//                Timber.d("Received data: $data")
//                return data
//            }
//            is ApiResponse.Error -> {
//                Timber.d("Error: ${response.code} - ${response.message}")
//                throw Error()
//            }
//        }
    }

    suspend fun addGroup(group: SnowbirdGroup): ApiResponse<String> {
        return client.sendRequest<String>("/api/groups", "POST", group)
    }
}