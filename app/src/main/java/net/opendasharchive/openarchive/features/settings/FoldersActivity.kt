package net.opendasharchive.openarchive.features.settings

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.adapters.FolderAdapter
import net.opendasharchive.openarchive.adapters.FolderAdapterListener
import net.opendasharchive.openarchive.databinding.ActivityFoldersBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.util.extensions.hide
import net.opendasharchive.openarchive.util.extensions.toggle

class FoldersActivity: BaseActivity(), FolderAdapterListener {

    companion object {
        const val EXTRA_SHOW_ARCHIVED = "show_archived"
    }

    private lateinit var mBinding: ActivityFoldersBinding
    private lateinit var mAdapter: FolderAdapter

    private var mArchived = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mArchived = intent.getBooleanExtra(EXTRA_SHOW_ARCHIVED, false)

        mBinding = ActivityFoldersBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.title = getString(if (mArchived) R.string.archived_folders else R.string.folders)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mAdapter = FolderAdapter(this)

        mBinding.rvProjects.layoutManager = LinearLayoutManager(this)
        mBinding.rvProjects.adapter = mAdapter

        mBinding.btViewArchived.toggle(!mArchived)
        mBinding.btViewArchived.setOnClickListener {
            val i = Intent(this, FoldersActivity::class.java)
            i.putExtra(EXTRA_SHOW_ARCHIVED, true)

            startActivity(i)
        }

        if (mArchived) {
            mBinding.cc.root.hide()
        }
        else {
            mBinding.cc.tvCc.setText(R.string.set_the_same_creative_commons_license_for_all_folders_on_this_server)

            CcSelector.init(mBinding.cc, Backend.current?.license) {
                val backend = Backend.current ?: return@init

                backend.license = it
                backend.save()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_browse_folder, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()

        val folders = if (mArchived) Backend.current?.archivedFolders else Backend.current?.folders

        mAdapter.update(folders ?: emptyList())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
//            R.id.action_add -> {
//                addFolder(mSelected)
//                return true
//            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun folderClicked(folder: Folder) {
        val i = Intent(this, EditFolderActivity::class.java)
        i.putExtra(EditFolderActivity.EXTRA_CURRENT_FOLDER_ID, folder.id)

        startActivity(i)
    }

    override fun getSelectedProject(): Folder? {
        return null
    }
}