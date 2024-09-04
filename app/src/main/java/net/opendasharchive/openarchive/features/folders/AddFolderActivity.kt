package net.opendasharchive.openarchive.features.folders

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityAddFolderBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.backends.BackendAdapter
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.backends.BackendViewModel
import net.opendasharchive.openarchive.features.backends.BackendViewModelFactory
import net.opendasharchive.openarchive.features.backends.ItemAction
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.util.AlertHelper
import net.opendasharchive.openarchive.util.SpacingItemDecoration
import timber.log.Timber

class AddFolderActivity : BaseActivity() {

    companion object {
        const val EXTRA_FOLDER_ID = "folder_id"
        const val EXTRA_FOLDER_NAME = "folder_name"
    }

    private lateinit var binding: ActivityAddFolderBinding
    private lateinit var recyclerView: RecyclerView
    private val viewModel: BackendViewModel by viewModels() {
        BackendViewModelFactory(BackendViewModel.Companion.Filter.CONNECTED)
    }
    private val adapter = BackendAdapter { backend, action ->
        when (action) {
            ItemAction.REQUEST_EDIT -> editBackend(backend)
            ItemAction.REQUEST_REMOVE -> deleteBackend(backend)
            ItemAction.SELECTED -> startActivity(Intent(this, CreateNewFolderActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create New Folder"

        createBackendList()

        binding.connectNewMediaServerButton.setOnClickListener {
            startActivity(Intent(this, BackendSetupActivity::class.java))
        }

//        val arrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_right)
//        arrow?.tint(ContextCompat.getColor(this, R.color.colorPrimary))

//        mBinding.newFolder.setDrawable(arrow, Position.End, tint = false)
//        mBinding.browseFolders.setDrawable(arrow, Position.End, tint = false)

        // We cannot browse the Internet Archive. Directly forward to creating a project,
        // as it doesn't make sense to show a one-option menu.
//        if (Folder.current?.backend?.tType == Backend.Type.INTERNET_ARCHIVE) {
//            mBinding.browseFolders.hide()
//            setFolder(false)
//            finish()
//        }
    }

    private fun editBackend(backend: Backend) {

    }

    private fun deleteBackend(backend: Backend) {
        val transition = AutoTransition().apply {
            duration = 500
            addTarget(recyclerView)
        }

        TransitionManager.beginDelayedTransition(recyclerView.parent as ViewGroup, transition)

        viewModel.deleteBackend(backend)
    }

    private fun createBackendList() {
        recyclerView = binding.backendList

        val spacingInPixels = resources.getDimensionPixelSize(R.dimen.list_item_spacing)
        binding.backendList.addItemDecoration(SpacingItemDecoration(spacingInPixels))

        binding.backendList.layoutManager = LinearLayoutManager(this)
        binding.backendList.adapter = adapter

        viewModel.backends.observe(this) { backends ->
            backends.forEach { backend ->
                Timber.d("Backend = ${backend.id} $backend")
            }
            adapter.submitList(backends)
        }
    }

    private fun removeInternetArchive() {
        val backend = Backend.get(type = Backend.Type.INTERNET_ARCHIVE).firstOrNull()

        if (backend != null) {
            AlertHelper.show(
                this,
                R.string.are_you_sure_you_want_to_remove_this_server_from_the_app,
                R.string.remove_from_app,
                buttons = listOf(
                    AlertHelper.positiveButton(R.string.remove) { _, _ ->
                        backend.delete()
                    },
                    AlertHelper.negativeButton()
                )
            )
        } else {
            Timber.d("Unable to find backend.")
        }
    }
}