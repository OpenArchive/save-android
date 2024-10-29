package net.opendasharchive.openarchive.services.snowbird

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.databinding.FragmentSnowbirdCreateRepoBinding
import net.opendasharchive.openarchive.db.SnowbirdError
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.db.SnowbirdRepo
import net.opendasharchive.openarchive.extensions.showKeyboard
import net.opendasharchive.openarchive.util.Utility
import timber.log.Timber

class SnowbirdCreateRepoFragment : BaseSnowbirdFragment() {

    private lateinit var viewBinding: FragmentSnowbirdCreateRepoBinding
    private lateinit var groupKey: String
    private lateinit var groupName: String
    private lateinit var repoName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            groupKey = it.getString("groupKey", "")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentSnowbirdCreateRepoBinding.inflate(inflater)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupName = SnowbirdGroup.get(groupKey)?.name ?: "Unknown Group"
        viewBinding.groupNameTextfield.setText(groupName)

        setupViewModelObservers()
        setupSideEffects()
    }

    private fun setupViewModelObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { snowbirdRepoViewModel.repoState.collect { state -> onRepoStateUpdate(state) } }
            }
        }
    }

    override fun handleError(error: SnowbirdError) {
        handleLoadingStatus(false)
        super.handleError(error)
    }

    private fun onRepoStateUpdate(state: SnowbirdRepoViewModel.RepoState) {
        Timber.d("state = $state")
        when (state) {
            is SnowbirdRepoViewModel.RepoState.Loading -> onLoading()
            is SnowbirdRepoViewModel.RepoState.SingleRepoSuccess -> onRepoCreated(state.groupKey, state.repo)
            is SnowbirdRepoViewModel.RepoState.Error -> handleError(state.error)
            else -> Unit
        }
    }

    private fun onLoading() {
        handleLoadingStatus(true)
    }

    private fun onRepoCreated(groupKey: String, repo: SnowbirdRepo) {
        repo.permissions = "READ_WRITE"
        repo.groupKey = groupKey
        repo.save()
        handleLoadingStatus(false)
        snowbirdRepoViewModel.fetchRepos(groupKey, false)
        Utility.showMaterialMessage(
            requireContext(),
            title = "Success!",
            message = "Successfully created ${repo.name}!") {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupSideEffects() {
        viewBinding.repoNameTextfield.post {
            viewBinding.repoNameTextfield.showKeyboard()
        }

        viewBinding.createRepoButton.setOnClickListener {
            repoName = viewBinding.repoNameTextfield.text?.toString().orEmpty()

            if (repoName.isBlank()) {
                viewBinding.repoNameTextfield.error = "Repository name cannot be empty"
            } else {
                snowbirdRepoViewModel.createRepo(groupKey, repoName)
                dismissKeyboard(it)
            }
        }
    }
}