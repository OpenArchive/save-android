package net.opendasharchive.openarchive.features.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.FragmentMainMediaBinding
import net.opendasharchive.openarchive.databinding.MediaGroupBinding
import net.opendasharchive.openarchive.db.Collection
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.db.MediaAdapter
import net.opendasharchive.openarchive.db.MediaViewHolder
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.util.extensions.cloak
import net.opendasharchive.openarchive.util.extensions.show
import net.opendasharchive.openarchive.util.extensions.toggle
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

    override fun onResume() {
        super.onResume()

        refresh()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mFolderId = arguments?.getLong(ARG_FOLDER_ID, -1) ?: -1

        mBinding = FragmentMainMediaBinding.inflate(inflater, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()
    }

    private fun refreshCurrentFolderCount() {
        Folder.current?.let { folder ->
            mBinding.currentFolder.currentFolderCount.text = NumberFormat.getInstance().format(
                folder.collections.map { it.size }
                    .reduceOrNull { acc, count -> acc + count } ?: 0)
            mBinding.currentFolder.currentFolderCount.show()
//            mBinding.uploadEditButton.toggle(project.isUploading)
        } ?: {
            mBinding.currentFolder.currentFolderCount.cloak()
//            mBinding.uploadEditButton.hide()
        }
    }

//    fun updateItem(collectionId: Long, mediaId: Long, progress: Long) {
//        mAdapters[collectionId]?.apply {
//            updateItem(mediaId, progress)
//            if (progress == -1L) {
//                updateHeader(collectionId, media)
//            }
//        }
//    }
//
//    private fun updateHeader(collectionId: Long, media: ArrayList<Media>) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            Collection.get(collectionId)?.let { collection ->
//                mCollections[collectionId] = collection
//                withContext(Dispatchers.Main) {
//                    mSection[collectionId]?.setHeader(collection, media)
//                }
//            }
//        }
//    }

    fun refresh() {
        setCurrentFolderState()
        refreshCurrentFolderCount()

        val folder = Folder.current ?: return

        mCollections = Collection.getByFolder(folder.id).associateBy { it.id }.toMutableMap()

        // Remove all sections for which collections don't exist anymore.
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
                val view = createMediaGroupView(collection, media)
                mBinding.mediaContainer.mediaContainerLayout.addView(view, 0)
            }
        }

        // DO NOT delete the collection here, this could lead to a race condition
        // while adding images.
        deleteCollections(toDelete, false)
    }

    private fun setCurrentFolderState() {
        Folder.current?.let { folder ->
            mBinding.currentFolder.currentBackendButton.icon = folder.backend?.getAvatar(requireContext())
            mBinding.currentFolder.currentBackendButton.visibility = View.VISIBLE
            mBinding.currentFolder.currentFolderCount.visibility = View.VISIBLE
            mBinding.addMediaHint.addMediaHint.toggle(mCollections.isEmpty())
            mBinding.currentFolder.currentBackendButton.text = getString(R.string.current_folder_label, folder.backend?.friendlyName, folder.name)

            mBinding.currentFolder.currentBackendButton.setOnClickListener {
                startActivity(Intent(context, BackendSetupActivity::class.java))
            }
        } ?: run {
            mBinding.currentFolder.currentBackendButton.visibility = View.GONE
            mBinding.currentFolder.currentFolderCount.visibility = View.GONE
            mBinding.addMediaHint.addMediaTitle.text = getString(R.string.tap_to_add_backend)
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

    private fun createMediaGroupView(collection: Collection, media: List<Media>): View {
        val holder = SectionViewHolder(MediaGroupBinding.inflate(layoutInflater))

        holder.recyclerView.layoutManager = GridLayoutManager(activity, COLUMN_COUNT)

        holder.setHeader(collection, media)

        val mediaAdapter = MediaAdapter(
            requireActivity(),
            { MediaViewHolder.SmallBox(it) },
            media,
            holder.recyclerView
        )

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
