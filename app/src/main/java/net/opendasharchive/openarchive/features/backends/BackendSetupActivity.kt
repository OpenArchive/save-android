package net.opendasharchive.openarchive.features.backends

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityBackendSetupBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.internetarchive.presentation.InternetArchiveActivity
import net.opendasharchive.openarchive.features.main.SwipeToDeleteCallback
import net.opendasharchive.openarchive.services.gdrive.GDriveActivity
import net.opendasharchive.openarchive.services.gdrive.GDriveConduit
import net.opendasharchive.openarchive.services.veilid.VeilidActivity
import net.opendasharchive.openarchive.services.webdav.WebDavActivity
import net.opendasharchive.openarchive.util.AlertHelper
import timber.log.Timber

class BackendSetupActivity : BaseActivity(), BackendAdapterListener {

    private lateinit var binding: ActivityBackendSetupBinding
    private val viewModel: BackendViewModel by viewModels()
    private lateinit var adapter: BackendAdapter
    private lateinit var mItemTouchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBackendSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Manage Media Servers"

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
    }

    private fun createBackendList() {
        adapter = BackendAdapter(this)

//        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
//        val drawable = AppCompatResources.getDrawable(this, R.drawable.separator)
//        divider.setDrawable(drawable!!)
//        binding.backendList.addItemDecoration(divider)

        val itemTouchHelper = createItemTouchHelper()
        itemTouchHelper.attachToRecyclerView(binding.backendList)

        binding.backendList.layoutManager = LinearLayoutManager(this)
        binding.backendList.adapter = adapter

        viewModel.backends.observe(this) { backends ->
            adapter.submitList(backends)
        }
    }

    private fun createItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(object : SwipeToDeleteCallback(this) {
            override fun isEditingAllowed(): Boolean {
                return true
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder): Boolean {
//                adapter?.onItemMove(
//                    viewHolder.bindingAdapterPosition,
//                    target.bindingAdapterPosition
//                )

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // adapter?.deleteItem(viewHolder.bindingAdapterPosition)
            }
        })
    }

    private fun showConfigScreenFor(backend: Backend) {
        when (backend.type) {
            Backend.Type.GDRIVE.id -> handleGoogle()
            Backend.Type.INTERNET_ARCHIVE.id -> startActivity(Intent(this, InternetArchiveActivity::class.java))
            Backend.Type.VEILID.id -> startActivity(Intent(this, VeilidActivity::class.java))
            Backend.Type.WEBDAV.id -> startActivity(Intent(this, WebDavActivity::class.java))
        }

    }

    override fun backendClicked(backend: Backend) {
        Timber.d("backendClicked")

        showConfigScreenFor(backend)
    }

    private fun handleGoogle() {
        val hasPerms = GDriveConduit.permissionsGranted(this)
        Timber.d("Permissions granted already? $hasPerms")

        if (hasPerms) {
            Timber.d("has perms")
            removeGoogle()
        } else {
            Timber.d("no perms")
            startActivity(Intent(this, GDriveActivity::class.java))
        }
    }

    private fun completeSignOut() {
        val googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)

        googleSignInClient.revokeAccess().addOnCompleteListener { result ->
            Timber.d("result = $result")

            if (result.isSuccessful) {
                googleSignInClient.signOut()
            }

            // Regardless of result, we need to remove GDrive from local config.
            //
            Backend.get(Backend.Type.GDRIVE).firstOrNull() { backend ->
                backend.delete()
            }
        }
    }

    private fun removeGoogle() {
        AlertHelper.show(this,
            R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
            R.string.remove_from_app,
            buttons = listOf(
            AlertHelper.positiveButton(R.string.remove) { _, _ ->
                completeSignOut()
            },
            AlertHelper.negativeButton()))
    }
}
