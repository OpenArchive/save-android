//import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
//import com.google.api.client.http.javanet.NetHttpTransport
//import com.google.api.client.json.gson.GsonFactory
//import com.google.api.services.drive.Drive
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.util.Collections
//
//class DriveAccessVerifier(private val clientId: String) {
//    private val jsonFactory = GsonFactory.getDefaultInstance()
//    private val transport = NetHttpTransport()
//
//    suspend fun verifyDriveAccess(idTokenString: String): Boolean = withContext(Dispatchers.IO) {
//        try {
//            val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
//                .setAudience(Collections.singletonList(clientId))
//                .build()
//
//            val idToken = verifier.verify(idTokenString)
//            if (idToken != null) {
//                val credential = GoogleCredential().setAccessToken(idToken.tokenValue)
//
//                val driveService = Drive.Builder(transport, jsonFactory, credential)
//                    .setApplicationName("Your App Name")
//                    .build()
//
//                val result = driveService.files().list()
//                    .setPageSize(1)
//                    .setFields("files(id, name)")
//                    .execute()
//
//                result.files?.isNotEmpty() ?: false
//            } else {
//                false
//            }
//        } catch (e: Exception) {
//            false
//        }
//    }
//}