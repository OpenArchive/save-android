package net.opendasharchive.openarchive.services

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

open class CommonServiceFragment : Fragment()  {
    companion object {
        const val RESP_CREATED = "created"
        const val RESP_CANCEL = "cancel"
        const val RESP_DELETED = "deleted"
    }

    lateinit var accountManager: AccountManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountManager = activity?.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    }

    fun accountExists(accountName: String, accountType: String): Boolean {
        val accounts = accountManager.getAccountsByType(accountType)
        return accounts.any { it.name == accountName }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun addNewAccountSuspend(
        accountType: String,
        authTokenType: String,
        requiredFeatures: Array<String>?,
        options: Bundle?
    ): Bundle = suspendCancellableCoroutine { continuation ->
        accountManager.addAccount(
            accountType,
            authTokenType,
            requiredFeatures,
            options,
            requireActivity(),
            { future ->
                try {
                    val result = future.result
                    continuation.resume(result)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            },
            null
        )
    }

    fun getAccount(accountName: String, accountType: String): Account? {
        val accounts = accountManager.getAccountsByType(accountType)
        return accounts.find { it.name == accountName }
    }

    fun removeAccount(account: Account) {
        accountManager.removeAccount(account, requireActivity(), null, null)
    }
}