package net.opendasharchive.openarchive.features.folders

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityBrowseFoldersBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.util.extensions.toggle
import timber.log.Timber


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

        setupRecyclerView()
        setupSwipeRefresh()

        try {
            viewModel.loadData(this)
        } catch (e: Error) {
            Timber.e(e)
        }

        viewModel.items.observe(this) { items ->
            Timber.d("Observed!")

            Timber.d("Loaded ${viewModel.items.value?.size} items")

            binding.foldersEmpty.toggle(items.isEmpty())

            // Stop the refreshing indicator
            binding.swipeRefreshLayout.isRefreshing = false

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

    private fun onFolderSelected(folder: Folder) {
        Timber.d("Selected folder!")

        if (folder != Folder.current) {
            Folder.current = folder
        }

        val intent = Intent()
        setResult(123, intent)
        finish()
    }

    private fun refreshFolders() {
        try {
            viewModel.loadData(this,  false)
        } catch (e: Error) {
            Timber.e(e)
            Toast.makeText(this, "Problem refreshing folders", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = BrowseFoldersAdapter() { folder ->
            onFolderSelected(folder)
        }

        binding.folderList.layoutManager = LinearLayoutManager(this)

        binding.folderList.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshFolders()
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark
        )
    }

    private fun addFolder() {
        startActivity(Intent(this, AddFolderActivity::class.java))

//        val backend = Folder.current?.backend ?: return
//
//        val license = backend.license

//        val folder = Folder(
//            description = "Foo",
//            created = Date(),
//            backend = backend,
//            licenseUrl = license)
//
//        folder.save()

//            val i = Intent()
//            i.putExtra(AddFolderActivity.EXTRA_FOLDER_ID, folder.id)
//            setResult(RESULT_OK, i)
//        }

//        finish()
    }
}