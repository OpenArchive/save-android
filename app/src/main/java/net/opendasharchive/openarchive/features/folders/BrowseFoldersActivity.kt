package net.opendasharchive.openarchive.features.folders

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityBrowseFoldersBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.util.extensions.toggle
import timber.log.Timber
import java.util.Date


class BrowseFoldersActivity : BaseActivity() {

    private lateinit var mBinding: ActivityBrowseFoldersBinding
    private lateinit var mViewModel: BrowseFoldersViewModel

    private var mSelected: BrowseFoldersViewModel.Folder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityBrowseFoldersBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mViewModel = BrowseFoldersViewModel()

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.browse_existing)

        mBinding.rvFolderList.layoutManager = LinearLayoutManager(this)

        val backend = Backend.current

        if (backend != null) {
            mViewModel.getFolders(this, backend)
        }

        mViewModel.folders.value?.forEach { it ->
            Timber.d("Folder: $it.name")
        }

        mViewModel.folders.observe(this) {
            mBinding.foldersEmpty.toggle(it.isEmpty())

            mBinding.rvFolderList.adapter = BrowseFoldersAdapter(it) { name ->
                mSelected = name
            }
        }

        mViewModel.progressBarFlag.observe(this) {
            mBinding.progressBar.toggle(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_browse_folder, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.action_add -> {
                addFolder(mSelected)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun addFolder(folder: BrowseFoldersViewModel.Folder?) {
        if (folder == null) return
        val backend = Backend.current ?: return

        // This should not happen. These should have been filtered on display.
        if (backend.hasFolder(folder.name)) return

        val license = backend.license

//        if (license.isNullOrBlank()) {
//            val i = Intent()
//            i.putExtra(AddFolderActivity.EXTRA_FOLDER_NAME, folder.name)
//
//            setResult(RESULT_CANCELED, i)
//        }
//        else {
            val folder = Folder(folder.name, Date(), backend.id, licenseUrl = license)
        folder.save()

            val i = Intent()
            i.putExtra(AddFolderActivity.EXTRA_FOLDER_ID, folder.id)

            setResult(RESULT_OK, i)
//        }

        finish()
    }
}