package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.FragmentBackendMetadataBinding
import net.opendasharchive.openarchive.features.folders.NewFolderNavigationAction
import net.opendasharchive.openarchive.features.folders.NewFolderNavigationViewModel
import net.opendasharchive.openarchive.features.settings.CcSelector
import net.opendasharchive.openarchive.util.Utility

class BackendMetadataFragment : Fragment() {
    private lateinit var binding: FragmentBackendMetadataBinding
    private val backendViewModel: BackendViewModel by activityViewModels()
    private val newFolderNavigationViewModel: NewFolderNavigationViewModel by activityViewModels()

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

        binding.createBackendButton.setOnClickListener {
            handleCreateButtonClicked()
        }

        newFolderNavigationViewModel.observeNavigation(viewLifecycleOwner) { action ->
            if (action == NewFolderNavigationAction.BackendMetadataCreated) {
                findNavController().navigate(BackendMetadataFragmentDirections.navigateToCreateNewFolderScreen())
            }
        }
    }

    private fun getLicenseUrl(): String {
        return backendViewModel.backend.value?.license ?: CcSelector.get(binding.cc) ?: ""
    }

    private fun handleCreateButtonClicked() {
        val license = getLicenseUrl()
        val nickname = binding.nickname.text.toString()

        viewLifecycleOwner.lifecycleScope.launch {
            backendViewModel.updateBackend { backend ->
                backend.license = license
                backend.nickname = nickname
            }
        }

        Utility.showMaterialMessage(
            requireContext(),
            title = "Congratulations!",
            message = "You can now add folders to your server.",
            positiveButtonText = "ADD FOLDERS") {

            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        newFolderNavigationViewModel.triggerNavigation(NewFolderNavigationAction.BackendMetadataCreated)
    }
}