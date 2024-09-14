package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentBackendSetupSuccessBinding
import net.opendasharchive.openarchive.features.folders.FolderViewModel
import timber.log.Timber

class BackendSetupSuccessFragment : Fragment() {
    private lateinit var mBinding: FragmentBackendSetupSuccessBinding
    private val folderViewModel: FolderViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragmentBackendSetupSuccessBinding.inflate(inflater)

        val backendName = getBackendName()

        mBinding.btAuthenticate.setOnClickListener { _ ->
            // findNavController().popBackStack(R.id.browse_folders_screen, false)
            clearBackStackAndNavigate(findNavController(), R.id.browse_folders_screen)
        }

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun getBackendName(): String {
        return folderViewModel.folder.value.backend?.friendlyName ?: "an unknown server"
    }

    private fun clearBackStackAndNavigate(navController: NavController, newDestinationId: Int) {
        // Get the start destination ID
        //
        val startDestId = navController.graph.startDestinationId

        // Create NavOptions to clear the back stack
        //
        val navOptions = NavOptions.Builder()
            .setPopUpTo(startDestId, true)
            .build()

        // Try to navigate with the NavOptions
        //
        try {
            navController.navigate(newDestinationId, null, navOptions)
            Timber.d("Successfully navigated up the stack")
        } catch (e: IllegalArgumentException) {
            // If navigation fails, fall back to manual clearing
            //
            Timber.d("Manually clearing backstack")
            clearEntireBackStack(navController)
            navController.navigate(newDestinationId)
        }
    }

    fun clearEntireBackStack(navController: NavController) {
        while (navController.popBackStack()) {
            // Keep popping until we can't pop anymore
        }
    }
}