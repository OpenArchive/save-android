package net.opendasharchive.openarchive.services.gdrive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentGdriveSignOutBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.services.CommonServiceFragment
import net.opendasharchive.openarchive.util.AlertHelper

class GDriveSignOutFragment : CommonServiceFragment() {
    private lateinit var binding: FragmentGdriveSignOutBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentGdriveSignOutBinding.inflate(inflater)

        binding.signOutButton.setOnClickListener {
            removeMyself()
        }

        return binding.root
    }

    private fun removeMyself() {
        AlertHelper.show(requireContext(), R.string.are_you_sure_you_want_to_remove_this_server_from_the_app, R.string.remove_from_app, buttons = listOf(
            AlertHelper.positiveButton(R.string.remove) { _, _ ->
                Backend.get(Backend.Type.GDRIVE).firstOrNull() { backend ->
                    // Google logout
                    val googleSignInClient = GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN)

                    googleSignInClient.revokeAccess().addOnCompleteListener {
                        googleSignInClient.signOut()
                    }

                    backend.delete()
                }
            },
            AlertHelper.negativeButton()))
    }
}