package net.opendasharchive.openarchive.services.snowbird

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import net.opendasharchive.openarchive.features.main.ApiResponse
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
    }

    suspend fun getGroups(): ApiResponse<SnowbirdGroup> {
        return client.sendRequest<SnowbirdGroup>(GROUPS_PATH, HttpMethod.GET)
    }

    suspend fun getGroup(key: String): ApiResponse<SnowbirdGroup> {
        return client.sendRequest<SnowbirdGroup>("$GROUPS_PATH/ovn1M8Qrw1f52onAEiLmglIiApJ32eAAFUuPewU1k8Q", HttpMethod.GET)
    }

    suspend fun createGroup(groupName: String): ApiResponse<SnowbirdGroup> {
        runBlocking {
            delay(2000)
        }
        val g = SnowbirdGroup(key = "foo")
        return ApiResponse.SingleResponse(g)
        //return client.sendRequest<RequestName, SnowbirdGroup>(GROUPS_PATH, HttpMethod.POST, RequestName(groupName))
    }
}