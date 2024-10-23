package net.opendasharchive.openarchive.features.main

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.opendasharchive.openarchive.db.SnowbirdRepo
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

//    fun testCreateRepo() = runTest {
//        val result = parseSnowbirdRepoResponse("blrSIdKPpLPlfJI6M9bTQFjW9BlnwboPzLQ-GPlJsGw", "r1")
//        assertEquals(ApiResponse.SingleResponse(repo), result)
//    }

//    @Test
//    fun testCustomUri() = runTest {
//        val uriString = "save+dweb::?dht=23571b8507645d9b5548fcd56ee088f7471e553a60d7b88d43edc6f4d1b7b59b&enc=054fd93e697963a660feaa1d4a0ec76083155f1270e24bb4c1eb3eb491d6c838&pk=bc33f7ceb83f45c4f6bf520417d1466271f4837b63d55aaab527c13e38ff78d3&sk=80431bbbb73d8204050259edd3dc0c438833e3b3f47f41d7600f1fb1552b1210&name=Pixel+6+Group"
//        val name = uriString.getQueryParameter("name")
//        assertEquals("Not dissecting URI strings correctly", "Pixel 6 Group", name)
//    }

//    @Suppress("SameParameterValue")
//    private fun parseSnowbirdRepoResponse(groupKey: String, repoName: String): ApiResponse<SnowbirdRepo> {
//        val client = UnixSocketClient()
//        val json = Json { ignoreUnknownKeys = true }
//        val jsonString = "{ \"key\":\"blrSIdKPpLPlfJI6M9bTQFjW9BlnwboPzLQ-GPlJsGw\", \"name\":\"r1\" }"
//
//        val obj = client.parseSuccessResponse(jsonString) { json.decodeFromString<SnowbirdRepo>(it) }
//
//        return when (obj) {
//            is ClientResponse.ErrorResponse -> ApiResponse.ErrorResponse(ApiError.UnexpectedError(obj.error.friendlyMessage))
//            is ClientResponse.SuccessResponse -> ApiResponse.SingleResponse(obj.data)
//        }
//    }
}