package net.opendasharchive.openarchive.features.folders

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentCreateNewFolderBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.settings.CcSelector
import net.opendasharchive.openarchive.services.webdav.ReadyToAuthTextWatcher
import net.opendasharchive.openarchive.util.Utility.showMaterialPrompt
import net.opendasharchive.openarchive.util.Utility.showMaterialWarning
import net.opendasharchive.openarchive.util.extensions.hide

class CreateNewFolderFragment : Fragment() {

    companion object {
        private const val SPECIAL_CHARS = ".*[\\\\/*\\s]"
    }

    private lateinit var binding: FragmentCreateNewFolderBinding
    private val newFolderNavigationViewModel: NewFolderNavigationViewModel by activityViewModels()
    private val newFolderDataViewModel: NewFolderDataViewModel by activityViewModels()
    private val navController by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateNewFolderBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.newFolderName.requestFocus()

        // binding.newFolderName.setText(intent.getStringExtra(BackendSelectionFragment.EXTRA_FOLDER_NAME))

        setFolderNameListeners()

        if (Folder.current?.backend?.license == null) {
            CcSelector.init(binding.cc, license = "https://creativecommons.org/licenses/by-sa/4.0")
        }
        else {
            binding.cc.root.hide()
        }

        binding.createFolderButton.setOnClickListener {
            store()
        }

        setupActions()
    }

    private fun enableIfReady() {
        val isComplete = !binding.newFolderName.text.isNullOrEmpty()

        binding.createFolderButton.isEnabled = isComplete
    }

    private fun setFolderNameListeners() {
        binding.newFolderName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                store()
            }

            false
        }

        binding.newFolderName.addTextChangedListener(object : ReadyToAuthTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                enableIfReady()
            }
        })
    }

    private fun setupActions() {
        newFolderNavigationViewModel.observeNavigation(viewLifecycleOwner) { action ->
            when (action) {
                is NewFolderNavigationAction.FolderCreated -> navController.navigate(CreateNewFolderFragmentDirections.navigationSegueSuccess())
                else -> Unit
            }
        }
    }

    private fun store() {
        val name = binding.newFolderName.text.toString()

        if (name.isBlank()) return

        if (name.matches(SPECIAL_CHARS.toRegex())) {
            showMaterialWarning(requireContext(), "Oops", getString(R.string.please_do_not_include_special_characters_in_the_name), "Ok")
            return
        }

        if (folderWithNameExists(name)) {
            showMaterialWarning(requireContext(), "Oops", getString(R.string.folder_name_already_exists), "Ok")
            return
        }

        updateWorkingFolder(name)

        saveFolder()

        showMaterialPrompt(
            requireContext(),
            "Folder Created!",
            "Would you like to make this your current folder?",
            "Yes", "No") { affirm ->

            if (affirm) {
                setFolderAsCurrent()
            }

            signalSuccess()
        }
    }

    private fun saveFolder() {
        lifecycleScope.launch {
            val folder = newFolderDataViewModel.folder.value
            folder.save()
        }
    }

    private fun setFolderAsCurrent() {
        lifecycleScope.launch {
            val folder = newFolderDataViewModel.folder.value
            Folder.current = folder
        }
    }

    private fun updateWorkingFolder(name: String) {
        newFolderDataViewModel.updateFolder { folder ->
            folder.copy(description = name)
        }
    }

    private fun folderWithNameExists(name: String): Boolean {
        val workingFolder = newFolderDataViewModel.folder.value

        workingFolder.backend?.let { backend ->
            val found = Folder.getLocalFoldersForBackend(backend).firstOrNull { it.description == name }
            return (found != null)
        }

        return false
    }

    private fun signalSuccess() {
        newFolderNavigationViewModel.triggerNavigation(NewFolderNavigationAction.FolderCreated)
    }
}
