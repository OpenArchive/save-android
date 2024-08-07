package net.opendasharchive.openarchive.features.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.esafirm.imagepicker.view.GridSpacingItemDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentMainMediaBinding
import net.opendasharchive.openarchive.databinding.MediaGroupBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.Collection
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.db.MediaAdapter
import net.opendasharchive.openarchive.db.MediaViewHolder
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.folders.BrowseFoldersActivity
import net.opendasharchive.openarchive.upload.BroadcastManager
import net.opendasharchive.openarchive.util.AlertHelper
import net.opendasharchive.openarchive.util.extensions.cloak
import net.opendasharchive.openarchive.util.extensions.show
import net.opendasharchive.openarchive.util.extensions.toggle
import timber.log.Timber
import java.text.NumberFormat
import kotlin.collections.set

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

    private var mAdapters = HashMap<Long, MediaAdapter>()
    private var mSection = HashMap<Long, SectionViewHolder>()
    private var mFolderId = -1L
    private var mCollections = mutableMapOf<Long, Collection>()

    private lateinit var mBinding: FragmentMainMediaBinding

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        private val handler = Handler(Looper.getMainLooper())
        override fun onReceive(context: Context, intent: Intent) {
            val action = BroadcastManager.getAction(intent) ?: return

            when (action) {
                BroadcastManager.Action.Change -> {
                    handler.post {
                        updateItem(action.collectionId, action.mediaId, action.progress)
                    }
                }

                BroadcastManager.Action.Delete -> {
                    handler.post {
                        refresh()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        BroadcastManager.register(requireContext(), mMessageReceiver)
    }

    override fun onStop() {
        super.onStop()
        BroadcastManager.unregister(requireContext(), mMessageReceiver)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_delete -> {
                AlertHelper.show(
                    requireContext(), R.string.confirm_remove_media, null, buttons = listOf(
                        AlertHelper.positiveButton(R.string.remove) { _, _ ->
                            deleteSelected()
                        },
                        AlertHelper.negativeButton()
                    )
                )
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mFolderId = arguments?.getLong(ARG_FOLDER_ID, -1) ?: -1

        mBinding = FragmentMainMediaBinding.inflate(inflater, container, false)

        val folder = getSelectedFolder()

        if (folder == null) {
            mBinding.currentFolder.root.visibility = View.GONE
        } else {
            val backendIcon = folder.backend?.getAvatar(requireContext())
            mBinding.currentFolder.currentBackendButton.icon = backendIcon
            mBinding.currentFolder.currentBackendButton.show()
            mBinding.currentFolder.currentBackendButton.text = folder.backend?.name ?: "Unknown storage"
            mBinding.currentFolder.currentFolderName.text = folder.description
            mBinding.currentFolder.currentFolderName.show()

            mBinding.currentFolder.currentBackendButton.setOnClickListener {
                Timber.d("CLICK BACKEND!")
                startActivity(Intent(context, BackendSetupActivity::class.java))
            }

            mBinding.currentFolder.currentFolderName.setOnClickListener {
                Timber.d("CLICK FOLDER!")
                startActivity(Intent(context, BrowseFoldersActivity::class.java))
            }
        }

        refreshCurrentFolderCount()

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()
    }

    private fun getSelectedFolder(): Folder? {
        return Backend.current?.folders?.firstOrNull()
    }

    private fun refreshCurrentFolderCount() {
        val folder = getSelectedFolder()

        if (folder != null) {
            mBinding.currentFolder.currentFolderCount.text = NumberFormat.getInstance().format(
                folder.collections.map { it.size }
                    .reduceOrNull { acc, count -> acc + count } ?: 0)
            mBinding.currentFolder.currentFolderCount.show()

//            mBinding.uploadEditButton.toggle(project.isUploading)
        } else {
            mBinding.currentFolder.currentFolderCount.cloak()
//            mBinding.uploadEditButton.hide()
        }
    }

    fun updateItem(collectionId: Long, mediaId: Long, progress: Long) {
        mAdapters[collectionId]?.apply {
            updateItem(mediaId, progress)
            if (progress == -1L) {
                updateHeader(collectionId, media)
            }
        }
    }

    private fun updateHeader(collectionId: Long, media: ArrayList<Media>) {
        lifecycleScope.launch(Dispatchers.IO) {
            Collection.get(collectionId)?.let { collection ->
                mCollections[collectionId] = collection
                withContext(Dispatchers.Main) {
                    mSection[collectionId]?.setHeader(collection, media)
                }
            }
        }
    }

    fun refresh() {
        mCollections = Collection.getByFolder(mFolderId).associateBy { it.id }.toMutableMap()

        // Remove all sections, which' collections don't exist anymore.
        val toDelete = mAdapters.keys.filter { id ->
            mCollections.containsKey(id).not()
        }.toMutableList()

        mCollections.forEach { (id, collection) ->
            val media = collection.media

            // Also remove all empty collections.
            if (media.isEmpty()) {
                toDelete.add(id)
                return@forEach
            }

            val adapter = mAdapters[id]
            val holder = mSection[id]

            if (adapter != null) {
                adapter.updateData(media)
                holder?.setHeader(collection, media)
            } else if (media.isNotEmpty()) {
                val view = createMediaList(collection, media)
                mBinding.mediaContainer.mediaContainer.addView(view, 0)
            }
        }

        // DO NOT delete the collection here, this could lead to a race condition
        // while adding images.
        deleteCollections(toDelete, false)

        mBinding.addMediaHint.addMediaHint.toggle(mCollections.isEmpty())
    }

    private fun deleteSelected() {
        val toDelete = ArrayList<Long>()

        mCollections.forEach { (id, collection) ->
            if (mAdapters[id]?.deleteSelected() == true) {
                val media = collection.media

                if (media.isEmpty()) {
                    toDelete.add(collection.id)
                } else {
                    mSection[id]?.setHeader(collection, media)
                }
            }
        }

        deleteCollections(toDelete, true)
    }

    private fun createMediaList(collection: Collection, media: List<Media>): View {
        val holder = SectionViewHolder(MediaGroupBinding.inflate(layoutInflater))

        val spacing = Math.round(15 * resources.displayMetrics.density)

        holder.recyclerView.setHasFixedSize(true)
        holder.recyclerView.layoutManager = GridLayoutManager(activity, COLUMN_COUNT)
        holder.recyclerView.addItemDecoration(GridSpacingItemDecoration(COLUMN_COUNT, spacing, false))

        holder.setHeader(collection, media)

        val mediaAdapter = MediaAdapter(
            requireActivity(),
            { MediaViewHolder.Box(it) },
            media,
            holder.recyclerView
        ) {
            (activity as? MainActivity)?.updateAfterDelete(mAdapters.values.firstOrNull { it.selecting } == null)
        }

        holder.recyclerView.adapter = mediaAdapter
        mAdapters[collection.id] = mediaAdapter
        mSection[collection.id] = holder

        return holder.root
    }

    private fun deleteCollections(collectionIds: List<Long>, cleanup: Boolean) {
        collectionIds.forEach { collectionId ->
            mAdapters.remove(collectionId)

            val holder = mSection.remove(collectionId)
            (holder?.root?.parent as? ViewGroup)?.removeView(holder.root)

            mCollections[collectionId]?.let {
                mCollections.remove(collectionId)
                if (cleanup) {
                    it.delete()
                }
            }
        }
    }
}
