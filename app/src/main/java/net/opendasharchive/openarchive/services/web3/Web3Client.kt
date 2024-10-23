package net.opendasharchive.openarchive.services.web3

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Serializable
data class UploadResult(
    val cid: String,
    val size: Long
)

@Serializable
data class Web3Space(
    val did: String,
    val name: String? = null
)

class Web3Client(
    private val webView: WebView,
    private val privateKey: String,
    private val space: Web3Space
) {
    private var isInitialized = false
    private val json = Json { ignoreUnknownKeys = true }

    init {
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // Enable ES6 module support
            javaScriptCanOpenWindowsAutomatically = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?) = true

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Initialize our JavaScript after page loads
                setupJavaScript()
            }
        }

        // Load a proper HTML page with module support
        webView.loadData("""
        <!DOCTYPE html>
        <html>
        <head>
            <script type="module">
                // Import the client
                import * as w3up from 'https://unpkg.com/@web3-storage/w3up-client/dist/src/index.js';
                
                // Make it globally available
                window.w3up = w3up;
                
                // Define our initialization function
                window.initializeWeb3 = async function(privateKey, space) {
                    try {
                        console.log('Creating web3 client...');
                        const client = await w3up.create();
                        
                        console.log('Setting current key...');
                        await client.setCurrentKey(privateKey);
                        
                        console.log('Getting spaces...');
                        const spaces = await client.spaces();
                        
                        console.log('Finding target space:', space);
                        const targetSpace = spaces.find(s => s.did() === space);
                        
                        if (!targetSpace) {
                            throw new Error('Space not found: ' + space);
                        }
                        
                        console.log('Setting current space...');
                        await client.setCurrentSpace(targetSpace);
                        
                        const result = {
                            did: targetSpace.did(),
                            name: targetSpace.name()
                        };
                        console.log('Initialization successful:', result);
                        return JSON.stringify(result);
                    } catch (e) {
                        console.error('Initialization failed:', e);
                        throw new Error('Failed to initialize: ' + e.message);
                    }
                }
            </script>
        </head>
        <body>
            <!-- WebView content -->
        </body>
        </html>
    """.trimIndent(), "text/html", "UTF-8")
    }

    private fun setupJavaScript() {
        // Add any additional JavaScript setup if needed
        webView.evaluateJavascript("""
        console.log('JavaScript setup complete');
    """.trimIndent(), null)
    }

    /**
     * Initializes the web3.storage client with the specified space
     * Must be called before any other operations
     * @return The initialized space information
     */
    suspend fun initialize(): Web3Space {
        if (isInitialized) {
            return getCurrentSpace()
        }

        return suspendCancellableCoroutine { continuation ->
            webView.evaluateJavascript(
                "initializeWeb3('$privateKey', '$space')"
            ) { result ->
                try {
                    val spaceInfo = json.decodeFromString<Web3Space>(result)
                    isInitialized = true
                    continuation.resume(spaceInfo)
                } catch (e: Exception) {
                    continuation.resumeWithException(
                        Exception("Failed to initialize web3.storage client: ${e.message}")
                    )
                }
            }
        }
    }


    /**
     * Uploads a file to w3up
     * @param file The file to upload
     * @return Flow<UploadProgress> containing progress updates and final result
     */
    fun uploadFile(file: File): Flow<UploadProgress> = flow {
        try {
            if (!isInitialized) throw Exception("Client not initialized11")
            if (!file.exists()) throw Exception("File does not exist")

            emit(UploadProgress.Preparing)

            // Get file mime type
            val mimeType = getMimeType(file) ?: "application/octet-stream"

            // Read file as base64
            val base64Data = file.readBytes().let {
                android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT)
            }

            emit(UploadProgress.Uploading(0))

            val result = suspendCancellableCoroutine { continuation ->
                webView.evaluateJavascript(
                    """
                    uploadToW3(
                        '$base64Data',
                        '${file.name}',
                        '$mimeType'
                    )
                    """.trimIndent()
                ) { resultJson ->
                    try {
                        val uploadResult = json.decodeFromString<UploadResult>(resultJson)
                        continuation.resume(uploadResult)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            }

            emit(UploadProgress.Success(result.cid))

        } catch (e: Exception) {
            Timber.e(e, "Upload failed")
            emit(UploadProgress.Error(e))
        }
    }

    private fun getMimeType(file: File): String? {
        return android.webkit.MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(file.extension)
    }

    /**
     * Gets the current space information
     * @throws Exception if client is not initialized
     */
    private suspend fun getCurrentSpace(): Web3Space {
        if (!isInitialized) throw Exception("Client not initialized33")

        return suspendCancellableCoroutine { continuation ->
            webView.evaluateJavascript(
                "JSON.stringify({ did: web3Client.space.current().did() })"
            ) { result ->
                try {
                    val spaceInfo = json.decodeFromString<Web3Space>(result)
                    continuation.resume(spaceInfo)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }
}

sealed class UploadProgress {
    data object Preparing : UploadProgress()
    data class Uploading(val percentage: Int) : UploadProgress()
    data class Success(val cid: String) : UploadProgress()
    data class Error(val exception: Exception) : UploadProgress()
}