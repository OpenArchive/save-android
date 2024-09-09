package net.opendasharchive.openarchive.features.backends

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityBackendSetupBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.folders.NewFolderNavigationViewModel
import timber.log.Timber


class BackendSetupActivity : BaseActivity() {

    private lateinit var binding: ActivityBackendSetupBinding
    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph
    private val newFolderNavigationViewModel: NewFolderNavigationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBackendSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Available Media Servers"

        if (savedInstanceState == null) {
            setupNavigation()
        }

//        newFolderViewModel.observeNavigation(this) { action ->
//            Timber.d("Action $action completed")
//
//            if (action == WizardNavigationAction.FolderCreated) {
//                Timber.d("YAY")
////                navController.navigate(R.id.browseFoldersFragment)
//            }
//        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.navController
        navGraph = navController.navInflater.inflate(R.navigation.backend_setup_navigation)

        setupActionBarWithNavController(navController)

        showTheCorrectFragment()
    }

    private fun showTheCorrectFragment() {
        Timber.d("is empty? ${Backend.getAll().isEmpty()}")

        navGraph.setStartDestination(if (Backend.getAll().isEmpty()) R.id.connectNewBackendFragment else R.id.browseFoldersFragment)
        navController.graph = navGraph
    }
}
