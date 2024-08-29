package net.opendasharchive.openarchive.features.folders

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityBrowseFoldersBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.util.extensions.toggle
import timber.log.Timber
import java.util.Date


class BrowseFoldersActivity : BaseActivity() {

    private lateinit var binding: ActivityBrowseFoldersBinding
    private lateinit var viewModel: BrowseFoldersViewModel
    private lateinit var adapter: BrowseFoldersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBrowseFoldersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = BrowseFoldersViewModel()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        title = getString(R.string.browse_existing)

        binding.folderList.layoutManager = LinearLayoutManager(this)

        try {
            viewModel.loadData(this)
        } catch (e: Error) {
            Timber.e(e)
        }

        adapter = BrowseFoldersAdapter() { folder ->
            Timber.d("Selected folder!")

            if (folder != Folder.current) {
                Folder.current = folder
            }

            val intent = Intent()
            setResult(123, intent)
            finish()
        }

        binding.folderList.adapter = adapter

        viewModel.items.observe(this) { items ->
            Timber.d("Observed!")

            Timber.d("Loaded ${viewModel.items.value?.size} items")

            binding.foldersEmpty.toggle(items.isEmpty())

            adapter.updateItems(items)

            invalidateOptionsMenu()
        }

        viewModel.progressBarFlag.observe(this) {
            binding.progressBar.toggle(it)
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
                addFolder()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun addFolder() {
        val backend = Folder.current?.backend ?: return

        // This should not happen. These should have been filtered on display.
//        if (backend.hasFolder(folder.description)) return

        val license = backend.license

//        if (license.isNullOrBlank()) {
//            val i = Intent()
//            i.putExtra(AddFolderActivity.EXTRA_FOLDER_NAME, folder.name)
//
//            setResult(RESULT_CANCELED, i)
//        }
//        else {
            val folder = Folder(
                description = "Foo",
                created = Date(),
                backend = backend,
                licenseUrl = license)
        folder.save()

//            val i = Intent()
//            i.putExtra(AddFolderActivity.EXTRA_FOLDER_ID, folder.id)
//
//            setResult(RESULT_OK, i)
//        }

        finish()
    }
}