package net.opendasharchive.openarchive.features.folders

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
            ItemAction.REQUEST_RENAME -> renameBackend(backend)
            ItemAction.REQUEST_DELETE -> deleteBackend(backend)
            ItemAction.SELECTED -> startActivity(Intent(this, CreateNewFolderActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Media Servers"

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

    private fun renameBackend(backend: Backend) {

    }

    private fun deleteBackend(backend: Backend) {
//        val initialHeight = binding.backendList.height
//        viewModel.deleteBackend(backend)
//        animateRecyclerViewHeight(initialHeight)

        val parentViewGroup = recyclerView.parent as? ViewGroup ?: return

        val transition = AutoTransition().apply {
            duration = 300
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

    private fun animateRecyclerViewHeight(initialHeight: Int) {
        Timber.d("Animating")

        recyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val targetHeight = if (adapter.itemCount > 0) {
                    recyclerView.computeVerticalScrollRange()
                } else {
                    0
                }

                ValueAnimator.ofInt(initialHeight, targetHeight).apply {
                    addUpdateListener { valueAnimator ->
                        val layoutParams = recyclerView.layoutParams
                        layoutParams.height = valueAnimator.animatedValue as Int
                        recyclerView.layoutParams = layoutParams
                    }
                    duration = 300
                    start()
                }
            }
        })
    }
}