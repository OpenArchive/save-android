package net.opendasharchive.openarchive.features.backends

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityBackendSelectionBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.backends.BackendViewModel.Companion.Filter
import net.opendasharchive.openarchive.features.core.BaseActivity

class BackendSelectionActivity : BaseActivity(), BackendAdapterListener {
    private lateinit var viewBinding: ActivityBackendSelectionBinding
    private lateinit var adapter: BackendAdapter
    private val viewModel: BackendViewModel by viewModels() {
        BackendViewModelFactory(Filter.CONNECTED)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityBackendSelectionBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Servers"

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_backend, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        finish()
                        true
                    }
                    R.id.new_backend_action -> {
                        startActivity(Intent(this@BackendSelectionActivity, BackendSetupActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        })

        configureViewBinding()
    }

    private fun configureViewBinding() {
        adapter = BackendAdapter(this)

        viewBinding.backendList.layoutManager = LinearLayoutManager(this)
        viewBinding.backendList.adapter = adapter

        viewModel.backends.observe(this) { backends ->
            adapter.submitList(backends)
        }
    }

    override fun onBackendClicked(backend: Backend) {
//        Backend.current = backend
//        finish()
    }
}