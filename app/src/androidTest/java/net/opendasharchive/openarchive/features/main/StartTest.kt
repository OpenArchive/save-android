package net.opendasharchive.openarchive.features.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import net.opendasharchive.openarchive.db.ApiError
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.services.snowbird.ApiResponse
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class StartTest {

    private val repo = SnowbirdRepo(
        key = "blrSIdKPpLPlfJI6M9bTQFjW9BlnwboPzLQ-GPlJsGw",
        name = "r1"
    )

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("net.opendasharchive.openarchive.debug", appContext.packageName)
    }

    @Test
    fun testCreateRepo() = runTest {
        val result = parseSnowbirdRepoResponse("blrSIdKPpLPlfJI6M9bTQFjW9BlnwboPzLQ-GPlJsGw", "r1")
        assertEquals(ApiResponse.SingleResponse(repo), result)
    }

    @Suppress("SameParameterValue")
    private fun parseSnowbirdRepoResponse(groupKey: String, repoName: String): ApiResponse<SnowbirdRepo> {
        val client = UnixSocketClient()
        val json = Json { ignoreUnknownKeys = true }
        val jsonString = "{ \"key\":\"blrSIdKPpLPlfJI6M9bTQFjW9BlnwboPzLQ-GPlJsGw\", \"name\":\"r1\" }"

        val obj = client.parseSuccessResponse(jsonString) { json.decodeFromString<SnowbirdRepo>(it) }

        return when (obj) {
            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(ApiError.UnexpectedError(obj.error.friendlyMessage))
            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(obj.data)
        }
    }
}