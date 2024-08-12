package net.opendasharchive.openarchive.services.filecoin

import android.content.Context
import com.eclipsesource.v8.V8
import com.eclipsesource.v8.V8Array
import com.eclipsesource.v8.V8Object
import timber.log.Timber
import java.io.IOException

class FilecoinJ2V8Manager(private val context: Context) {

    private lateinit var v8: V8
    private lateinit var filecoinJs: V8Object
    private lateinit var walletProvider: V8Object

    fun initialize() {
        v8 = V8.createV8Runtime()

        // Load Filecoin.js
        try {
            // Load polyfill first
            val polyfillContent = context.assets.open("polyfill.js").bufferedReader().use { it.readText() }
            v8.executeVoidScript(polyfillContent)

            val filecoinJsContent = context.assets.open("filecoin.js").bufferedReader().use { it.readText() }
            v8.executeVoidScript(filecoinJsContent)
            val keys = v8.keys
            filecoinJs = v8.getObject("FilecoinJs")
            walletProvider = filecoinJs.getObject("LightWalletProvider")
            Timber.d("Created filecoinJSs")
        } catch (e: IOException) {
            throw RuntimeException("Failed to load Filecoin.js", e)
        }
    }

    fun createWallet(): String {
        try {
            val f = walletProvider.keys
            val result = walletProvider.executeStringFunction("createWallet", null)
            return result ?: throw RuntimeException("Failed to create wallet")
        } catch (e: Exception) {
            Timber.e("Error creating wallet", e)
            throw RuntimeException("Failed to create wallet", e)
        }
    }

    fun getBalance(address: String): Double {
        val params = V8Array(v8).push(address)
        try {
            val result = filecoinJs.executeDoubleFunction("getBalance", params)
            return result
        } finally {
            // params.release()
        }
    }

    fun createDeal(minerAddress: String, dataCid: String, price: Double, duration: Int): String {
        val params = V8Array(v8).push(minerAddress).push(dataCid).push(price).push(duration)
        try {
            val result = filecoinJs.executeStringFunction("createDeal", params)
            return result ?: throw RuntimeException("Failed to create deal")
        } finally {
            // params.release()
        }
    }
}