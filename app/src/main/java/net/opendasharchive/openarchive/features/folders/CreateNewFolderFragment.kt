package net.opendasharchive.openarchive.features.folders

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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
    private val newFolderViewModel: NewFolderViewModel by activityViewModels()
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
        newFolderViewModel.observeNavigation(viewLifecycleOwner) { action ->
            when (action) {
                is WizardNavigationAction.FolderCreated -> navController.navigate(CreateNewFolderFragmentDirections.navigationSegueSuccess())
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

        val folder = newFolderViewModel.folder

        if (folder.description == name) {
            showMaterialWarning(requireContext(), "Oops", getString(R.string.folder_name_already_exists), "Ok")
            return
        }

        folder.description = name

        folder.licenseUrl = folder.backend?.license ?: CcSelector.get(binding.cc)

        folder.save()

        showMaterialPrompt(
            requireContext(),
            "Folder Created!",
            "Would you like to make this your current folder?",
            "Yes", "No") { affirm ->

            if (affirm) {
                Folder.current = folder
            }

            signalSuccess()
        }
    }

    private fun signalSuccess() {
        newFolderViewModel.triggerNavigation(WizardNavigationAction.FolderCreated)
    }
}
