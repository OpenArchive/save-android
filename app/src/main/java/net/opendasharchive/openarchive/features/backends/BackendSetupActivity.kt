package net.opendasharchive.openarchive.features.backends

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.databinding.ActivityBackendSetupBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.internetarchive.presentation.InternetArchiveActivity
import net.opendasharchive.openarchive.services.gdrive.GDriveActivity
import net.opendasharchive.openarchive.services.veilid.VeilidActivity
import net.opendasharchive.openarchive.services.webdav.WebDavActivity
import timber.log.Timber

class BackendSetupActivity : BaseActivity(), BackendAdapterListener {

    private lateinit var binding: ActivityBackendSetupBinding
    private val viewModel: BackendViewModel by viewModels()
    private lateinit var adapter: BackendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBackendSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Media Storage"

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        })

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        createBackendList()

//        initBackendSetupFragmentBindings()
//        initBackendSetupSuccessFragmentBindings()
    }

    private fun createBackendList() {
        adapter = BackendAdapter(this)

        binding.backendList.layoutManager = LinearLayoutManager(this)
        binding.backendList.adapter = adapter

        viewModel.backends.observe(this) { backends ->
            adapter.submitList(backends)
        }
    }

    private fun showConfigScreenFor(backend: Backend) {
        when (backend.type) {
            Backend.Type.GDRIVE.id -> startActivity(Intent(this, GDriveActivity::class.java))
            Backend.Type.INTERNET_ARCHIVE.id -> startActivity(Intent(this, InternetArchiveActivity::class.java))
            Backend.Type.VEILID.id -> startActivity(Intent(this, VeilidActivity::class.java))
            Backend.Type.WEBDAV.id -> startActivity(Intent(this, WebDavActivity::class.java))
        }

    }

    override fun backendClicked(backend: Backend) {
        Timber.d("backendClicked")

        showConfigScreenFor(backend)
    }
}
