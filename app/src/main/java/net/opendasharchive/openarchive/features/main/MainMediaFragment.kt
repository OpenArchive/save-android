package net.opendasharchive.openarchive.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.WorkInfo
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentMainMediaBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.MediaViewModel
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.main.ui.GridSectionAdapter
import net.opendasharchive.openarchive.features.main.ui.GridSectionLayoutDecoration
import net.opendasharchive.openarchive.features.main.ui.GridSectionViewModel
import net.opendasharchive.openarchive.features.main.ui.SectionedGridLayoutManager
import net.opendasharchive.openarchive.upload.MediaUploadViewModel
import net.opendasharchive.openarchive.util.extensions.cloak
import net.opendasharchive.openarchive.util.extensions.show
import net.opendasharchive.openarchive.util.extensions.toggle
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.text.NumberFormat

class MainMediaFragment : Fragment() {

    companion object {
        private const val COLUMN_COUNT = 4
        private const val ARG_FOLDER_ID = "folder_id"

        fun newInstance(folderId: Long): MainMediaFragment {
            val args = Bundle()
            args.putLong(ARG_FOLDER_ID, folderId)

            val fragment = MainMediaFragment()
            fragment.arguments = args

            return fragment
        }
    }

//    private var mAdapters = HashMap<Long, MediaAdapter>()
//    private var mSection = HashMap<Long, SectionViewHolder>()
//    private var mFolderId = -1L
//    private var mCollections = mutableMapOf<Long, Collection>()

//    private val mediaUploadViewModel: MediaUploadViewModel by activityViewModels() {
//        MediaUploadViewModelFactory(MediaUploadRepository(MediaUploadManager))
//    }
    private val mediaViewModel: MediaViewModel by viewModel()
    private val mediaUploadViewModel: MediaUploadViewModel by viewModel()
    private val gridSectionViewModel: GridSectionViewModel by viewModel()
    private lateinit var adapter: GridSectionAdapter
    private lateinit var viewBinding: FragmentMainMediaBinding

    override fun onResume() {
        super.onResume()

        refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            gridSectionViewModel.loadItems()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        mFolderId = arguments?.getLong(ARG_FOLDER_ID, -1) ?: -1

        viewBinding = FragmentMainMediaBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModels()

        mediaUploadViewModel.uploadItems.observe(viewLifecycleOwner) { mediaUploadItems ->
            Timber.d("media upload status changed: $mediaUploadItems")

            mediaUploadItems.forEach { mediaItem ->
                when (mediaItem.workInfo?.state) {
                    WorkInfo.State.ENQUEUED -> Timber.d("queued")
                    WorkInfo.State.RUNNING -> Timber.d("running")
                    WorkInfo.State.SUCCEEDED -> Timber.d("succeeded")
                    WorkInfo.State.FAILED -> Timber.d("failed")
                    WorkInfo.State.BLOCKED -> Timber.d("blocked")
                    WorkInfo.State.CANCELLED -> Timber.d("cancelled")
                    null -> Unit
                }
            }
        }

        refresh()
    }

    private fun setupRecyclerView() {
        adapter = GridSectionAdapter(gridSectionViewModel.selectedItems) { position, isSelected ->
            Timber.d("Cleeek !")
        }

        val layoutManager = SectionedGridLayoutManager(requireContext(), 4, adapter)
        viewBinding.recyclerView.layoutManager = layoutManager
        viewBinding.recyclerView.adapter = adapter

        val sectionSpacing = resources.getDimensionPixelSize(R.dimen.grid_layout_section_spacing)
        val headerBottomMargin = resources.getDimensionPixelSize(R.dimen.grid_layout_header_bottom_margin)
        val itemTopMargin = resources.getDimensionPixelSize(R.dimen.grid_layout_item_top_margin)
        viewBinding.recyclerView.addItemDecoration(GridSectionLayoutDecoration(sectionSpacing, headerBottomMargin, itemTopMargin))
    }

    private fun observeViewModels() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    gridSectionViewModel.items.collect { gridSectionItems ->
                        Timber.d("grid view model changed: ${gridSectionItems.size}")
                        adapter.submitList(gridSectionItems)
                    }
                }
                launch {
                    mediaViewModel.saveFlow.collect { savedMedia ->
                        Timber.d("Media saved: $savedMedia")
                        gridSectionViewModel.addNewMedia(savedMedia)
                    }
                }
            }
        }
    }

    private fun refreshCurrentFolderCount() {
        Folder.current?.let { folder ->
            viewBinding.currentFolder.currentFolderCount.text = NumberFormat.getInstance().format(
                folder.collections.map { it.size }
                    .reduceOrNull { acc, count -> acc + count } ?: 0)
            viewBinding.currentFolder.currentFolderCount.show()
//            viewBinding.uploadEditButton.toggle(project.isUploading)
        } ?: {
            viewBinding.currentFolder.currentFolderCount.cloak()
//            viewBinding.uploadEditButton.hide()
        }
    }

    fun refresh() {
        setCurrentFolderState()
        refreshCurrentFolderCount()

//        val folder = Folder.current ?: return
//
//        mCollections = Collection.getByFolder(folder.id).associateBy { it.id }.toMutableMap()
//
//        // Remove all sections for which collections don't exist anymore.
//        val toDelete = mAdapters.keys.filter { id ->
//            mCollections.containsKey(id).not()
//        }.toMutableList()
//
//        mCollections.forEach { (id, collection) ->
//            val media = collection.media
//
//            // Also remove all empty collections.
//            if (media.isEmpty()) {
//                toDelete.add(id)
//                return@forEach
//            }
//
//            val adapter = mAdapters[id]
//            val holder = mSection[id]
//
//            if (adapter != null) {
//                adapter.updateData(media)
//                holder?.setHeader(collection, media)
//            } else if (media.isNotEmpty()) {
//                val view = createMediaGroupView(collection, media)
//                viewBinding.mediaContainer.mediaContainerLayout.addView(view, 0)
//            }
//        }

        // DO NOT delete the collection here, this could lead to a race condition
        // while adding images.
//        deleteCollections(toDelete, false)
    }

    private fun setCurrentFolderState() {
        Folder.current?.let { folder ->
            viewBinding.currentFolder.currentBackendButton.icon = folder.backend?.getAvatar(requireContext())
            viewBinding.currentFolder.currentBackendButton.visibility = View.VISIBLE
            viewBinding.currentFolder.currentFolderCount.visibility = View.VISIBLE
            viewBinding.addMediaHint.addMediaHint.toggle(false)
            viewBinding.currentFolder.currentBackendButton.text = getString(R.string.current_folder_label, folder.backend?.friendlyName, folder.name)

            viewBinding.currentFolder.currentBackendButton.setOnClickListener {
                startActivity(Intent(context, BackendSetupActivity::class.java))
            }
        } ?: run {
            viewBinding.currentFolder.currentBackendButton.visibility = View.GONE
            viewBinding.currentFolder.currentFolderCount.visibility = View.GONE
            viewBinding.addMediaHint.addMediaTitle.text = getString(R.string.tap_to_add_backend)
        }
    }

//    private fun deleteSelected() {
//        val toDelete = ArrayList<Long>()
//
//        mCollections.forEach { (id, collection) ->
//            if (mAdapters[id]?.deleteSelected() == true) {
//                val media = collection.media
//
//                if (media.isEmpty()) {
//                    toDelete.add(collection.id)
//                } else {
//                    mSection[id]?.setHeader(collection, media)
//                }
//            }
//        }
//
//        deleteCollections(toDelete, true)
//    }

//    private fun createMediaGroupView(collection: Collection, media: List<Media>): View {
//        val holder = SectionViewHolder(MediaGroupBinding.inflate(layoutInflater))
//
//        holder.recyclerView.layoutManager = GridLayoutManager(activity, COLUMN_COUNT)
//
//        holder.setHeader(collection, media)
//
//        val mediaAdapter = MediaAdapter(
//            requireActivity(),
//            { MediaViewHolder.SmallBox(it) },
//            media,
//            holder.recyclerView
//        )
//
//        holder.recyclerView.adapter = mediaAdapter
//        mAdapters[collection.id] = mediaAdapter
//        mSection[collection.id] = holder
//
//        return holder.root
//    }

//    private fun deleteCollections(collectionIds: List<Long>, cleanup: Boolean) {
//        collectionIds.forEach { collectionId ->
//            mAdapters.remove(collectionId)
//
//            val holder = mSection.remove(collectionId)
//            (holder?.root?.parent as? ViewGroup)?.removeView(holder.root)
//
//            mCollections[collectionId]?.let {
//                mCollections.remove(collectionId)
//                if (cleanup) {
//                    it.delete()
//                }
//            }
//        }
//    }
}
