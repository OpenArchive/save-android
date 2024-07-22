package net.opendasharchive.openarchive.features.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentSpaceSetupBinding
import net.opendasharchive.openarchive.db.Space
import net.opendasharchive.openarchive.features.main.MainActivity
import net.opendasharchive.openarchive.util.AlertHelper
import net.opendasharchive.openarchive.util.extensions.hide

class SpaceSetupFragment : Fragment() {

    private lateinit var mBinding: FragmentSpaceSetupBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragmentSpaceSetupBinding.inflate(inflater)

        mBinding.skipForNowButton.visibility = View.GONE

        if (Space.has(Space.Type.WEBDAV)) {
            mBinding.privateServerSublabel.text = "Connected"
            mBinding.iconNextPrivateServer.setIconResource(R.drawable.outline_link_off_24)
        } else {
            mBinding.privateServerSublabel.text = "Not connected"
            mBinding.iconNextPrivateServer.setIconResource(R.drawable.outline_add_link_24)
            mBinding.iconNextPrivateServer.setOnClickListener {
                setFragmentResult(RESULT_REQUEST_KEY, bundleOf(RESULT_BUNDLE_KEY to RESULT_VAL_WEBDAV))
            }
        }

        if (Space.has(Space.Type.INTERNET_ARCHIVE)) {
            mBinding.internetArchiveSublabel.text = "Connected"
            mBinding.iconNextInternetArchive.setIconResource(R.drawable.outline_link_off_24)
            mBinding.iconNextInternetArchive.setOnClickListener {
                removeInternetArchive()
            }
        } else {
            mBinding.internetArchiveSublabel.text = "Not connected"
            mBinding.iconNextInternetArchive.setIconResource(R.drawable.outline_add_link_24)
            mBinding.iconNextInternetArchive.setOnClickListener {
                setFragmentResult(RESULT_REQUEST_KEY, bundleOf(RESULT_BUNDLE_KEY to RESULT_VAL_INTERNET_ARCHIVE))
            }
        }

        if (!playServicesAvailable()) {
            mBinding.gdrive.hide()
        } else {
            if (Space.has(Space.Type.GDRIVE)) {
                mBinding.gdriveSublabel.text = "Connected"
                mBinding.iconNextGdrive.setIconResource(R.drawable.outline_link_off_24)
                mBinding.iconNextGdrive.setOnClickListener {
                    removeGoogleSpace()
                }
            } else {
                mBinding.gdriveSublabel.text = "Not connected"
                mBinding.iconNextGdrive.setIconResource(R.drawable.outline_add_link_24)
                mBinding.iconNextGdrive.setOnClickListener {
                    setFragmentResult(RESULT_REQUEST_KEY, bundleOf(RESULT_BUNDLE_KEY to RESULT_VAL_GDRIVE))
                }
            }
        }

        mBinding.skipForNowButton.setOnClickListener {
            skipSpaceConfig()
        }

        return mBinding.root
    }

    private fun removeInternetArchive() {
        Space.get(Space.Type.INTERNET_ARCHIVE.id).let { space ->
            AlertHelper.show(
                requireContext(),
                R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
                R.string.remove_from_app,
                buttons = listOf(
                    AlertHelper.positiveButton(R.string.remove) { _, _ ->
                        space?.delete()
                        view?.invalidate()
                    },
                    AlertHelper.negativeButton()
                )
            )
        }
    }

    private fun removeGoogleSpace() {
        Space.get(Space.Type.GDRIVE.id).also { space ->
            AlertHelper.show(
                requireContext(),
                R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
                R.string.remove_from_app,
                buttons = listOf(
                    AlertHelper.positiveButton(R.string.remove) { _, _ ->
                        val googleSignInClient = GoogleSignIn.getClient(
                            requireActivity(),
                            GoogleSignInOptions.DEFAULT_SIGN_IN
                        )

                        googleSignInClient.revokeAccess().addOnCompleteListener {
                            googleSignInClient.signOut()
                            space?.delete()
                        }
                    },
                    AlertHelper.negativeButton()
                )
            )
        }
    }

    private fun skipSpaceConfig() {
        startActivity(Intent(context, MainActivity::class.java))
    }

    private fun playServicesAvailable(): Boolean {
        return ConnectionResult.SUCCESS == GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(requireContext())
    }

    companion object {
        const val RESULT_REQUEST_KEY = "space_setup_fragment_result"
        const val RESULT_BUNDLE_KEY = "space_setup_result_key"
        const val RESULT_VAL_WEBDAV = "webdav"
        const val RESULT_VAL_INTERNET_ARCHIVE = "internet_archive"
        const val RESULT_VAL_GDRIVE = "gdrive"
    }
}