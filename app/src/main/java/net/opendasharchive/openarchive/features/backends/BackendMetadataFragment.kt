package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import net.opendasharchive.openarchive.databinding.FragmentBackendMetadataBinding
import net.opendasharchive.openarchive.features.folders.NewFolderViewModel
import net.opendasharchive.openarchive.features.folders.WizardNavigationAction
import net.opendasharchive.openarchive.features.settings.CcSelector
import net.opendasharchive.openarchive.util.Analytics

class BackendMetadataFragment : Fragment() {
    private lateinit var binding: FragmentBackendMetadataBinding
    private val newFolderViewModel: NewFolderViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBackendMetadataBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    fun setup() {
        CcSelector.init(binding.cc, license = "https://creativecommons.org/licenses/by-sa/4.0")

        binding.authenticationButton.setOnClickListener {
            val backend = newFolderViewModel.backend

            binding.nickname.text?.let { nickname ->
                if (nickname.isNotEmpty()) {
                    backend.name = nickname.toString()
                }
            }

            backend.license = CcSelector.get(binding.cc)

            newFolderViewModel.backend = backend

            // Mixing "name" and "type" here, but it should make more sense
            // for the humans reading the logs.
            //
            Analytics.log(Analytics.NEW_BACKEND_CONNECTED, mutableMapOf("type" to backend.name))

            signalSuccess()
        }

        newFolderViewModel.observeNavigation(viewLifecycleOwner) { action ->
            if (action == WizardNavigationAction.FolderMetadataCreated) {
                findNavController().navigate(BackendMetadataFragmentDirections.navigationSegueToFolderCreation())
            }
        }
    }

    private fun signalSuccess() {
        newFolderViewModel.triggerNavigation(WizardNavigationAction.FolderMetadataCreated)
    }
}