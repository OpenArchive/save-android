package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import net.opendasharchive.openarchive.databinding.FragmentBackendMetadataBinding
import net.opendasharchive.openarchive.features.folders.NewFolderDataViewModel
import net.opendasharchive.openarchive.features.folders.NewFolderNavigationAction
import net.opendasharchive.openarchive.features.folders.NewFolderNavigationViewModel
import net.opendasharchive.openarchive.features.settings.CcSelector

class BackendMetadataFragment : Fragment() {
    private lateinit var binding: FragmentBackendMetadataBinding
    private val newFolderDataViewModel: NewFolderDataViewModel by activityViewModels()
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

        binding.authenticationButton.setOnClickListener {
            handleOkButtonClicked()
        }

        newFolderNavigationViewModel.observeNavigation(viewLifecycleOwner) { action ->
            if (action == NewFolderNavigationAction.FolderMetadataCreated) {
                findNavController().navigate(BackendMetadataFragmentDirections.navigationSegueToFolderCreation())
            }
        }
    }

    private fun getLicenseUrl(): String {
        return newFolderDataViewModel.folder.value.backend?.license ?: CcSelector.get(binding.cc) ?: ""
    }

    private fun handleOkButtonClicked() {
        val license = getLicenseUrl()
        val nickname = binding.nickname.text.toString()

        updateWorkingBackend(nickname, license)

        signalSuccess()
    }

    private fun signalSuccess() {
        newFolderNavigationViewModel.triggerNavigation(NewFolderNavigationAction.FolderMetadataCreated)
    }

    private fun updateWorkingBackend(nickname: String, license: String) {
        newFolderDataViewModel.updateBackendNickname(nickname)
        newFolderDataViewModel.updateBackendLicense(license)
    }
}