package net.opendasharchive.openarchive.features.folders

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import net.opendasharchive.openarchive.databinding.ActivityAddFolderBinding
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.features.backends.BackendAdapter
import net.opendasharchive.openarchive.features.backends.BackendAdapterListener
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.features.backends.BackendViewModel
import net.opendasharchive.openarchive.features.backends.BackendViewModelFactory
import net.opendasharchive.openarchive.features.core.BaseActivity
import timber.log.Timber

class AddFolderActivity : BaseActivity(), BackendAdapterListener {

    companion object {
        const val EXTRA_FOLDER_ID = "folder_id"
        const val EXTRA_FOLDER_NAME = "folder_name"
    }

//    private lateinit var mResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivityAddFolderBinding
    private val viewModel: BackendViewModel by viewModels() {
        BackendViewModelFactory(BackendViewModel.Companion.Filter.CONNECTED)
    }
    private lateinit var adapter: BackendAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        mResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//            if (it.resultCode == RESULT_OK) {
//                setResult(RESULT_OK, it.data)
//                finish()
//            }
//            else {
//                val name = it.data?.getStringExtra(EXTRA_FOLDER_NAME)
//
//                if (!name.isNullOrBlank()) {
//                    val i = Intent(this, CreateNewFolderActivity::class.java)
//                    i.putExtra(EXTRA_FOLDER_NAME, name)
//
//                    mResultLauncher.launch(i)
//                }
//            }
//        }

        binding = ActivityAddFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add New Folder"

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

    private fun createBackendList() {
        adapter = BackendAdapter(this)

        binding.backendList.setHasFixedSize(true)
        binding.backendList.layoutManager = LinearLayoutManager(this)
        binding.backendList.adapter = adapter

        viewModel.backends.observe(this) { backends ->
            adapter.submitList(backends)
        }
    }

    override fun onBackendClicked(backend: Backend) {
        Timber.d("Backend clicked!")
        startActivity(Intent(this, CreateNewFolderActivity::class.java))
    }
}