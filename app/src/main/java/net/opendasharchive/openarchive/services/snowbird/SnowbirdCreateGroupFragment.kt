package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdCreateGroupBinding
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.util.Utility

class SnowbirdCreateGroupFragment : BaseSnowbirdFragment() {

    private lateinit var viewBinding: FragmentSnowbirdCreateGroupBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdCreateGroupBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.createGroupButton.setOnClickListener {
            snowbirdGroupViewModel.createGroup(viewBinding.groupNameTextfield.text.toString())
            dismissKeyboard(it)
        }

        initializeViewModelObservers()
    }

    private fun initializeViewModelObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { snowbirdGroupViewModel.isProcessing.collect { isProcessing -> handleLoadingStatus(isProcessing) } }
                launch { snowbirdGroupViewModel.groupState.collect { state -> handleGroupStateUpdate(state) } }
                launch { snowbirdRepoViewModel.repoState.collect { state -> handleRepoStateUpdate(state) } }
            }
        }
    }

    private fun handleGroupStateUpdate(state: SnowbirdGroupViewModel.GroupState) {
        when (state) {
            is SnowbirdGroupViewModel.GroupState.Idle -> { /* Initial state */ }
            is SnowbirdGroupViewModel.GroupState.Loading -> handleLoadingStatus(true)
            is SnowbirdGroupViewModel.GroupState.SingleGroupSuccess -> handleGroupCreated(state.group)
            is SnowbirdGroupViewModel.GroupState.Error -> handleError(state.error)
            else -> Unit
        }
    }

    private fun handleRepoStateUpdate(state: SnowbirdRepoViewModel.RepoState) {
        when (state) {
            is SnowbirdRepoViewModel.RepoState.Idle -> { /* Initial state */ }
            is SnowbirdRepoViewModel.RepoState.Loading -> handleLoadingStatus(true)
            is SnowbirdRepoViewModel.RepoState.SingleRepoSuccess -> handleRepoCreated(state.repo)
            is SnowbirdRepoViewModel.RepoState.Error -> handleError(state.error)
            else -> Unit
        }
    }

    private fun handleGroupCreated(group: SnowbirdGroup?) {
        group?.let {
            snowbirdGroupViewModel.setCurrentGroup(group)

            lifecycleScope.launch {
                group.save()
                snowbirdRepoViewModel.createRepo(
                    group.key, viewBinding.repoNameTextfield.text.toString()
                )
            }
        }
    }

    private fun handleRepoCreated(repo: SnowbirdRepo?) {
        repo?.let {
            repo.snowbirdGroup = snowbirdGroupViewModel.currentGroup.value
            repo.save()
            showConfirmation(repo)
        }
    }

    private fun showConfirmation(repo: SnowbirdRepo?) {
        val group = repo?.snowbirdGroup ?: return

        Utility.showMaterialPrompt(
            requireContext(),
            title = "Snowbird Group Created",
            message = "Would you like to share your new group with a QR code?",
            positiveButtonText = "Yes",
            negativeButtonText = "No") { affirm ->
            if (affirm) {
                findNavController().navigate(SnowbirdCreateGroupFragmentDirections.navigateToShareScreen(group.key))
            } else {
                parentFragmentManager.popBackStack()
            }
        }
    }
}