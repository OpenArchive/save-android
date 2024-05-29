package net.opendasharchive.openarchive.features.folders

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityAddFolderBinding
import net.opendasharchive.openarchive.db.Space
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.onboarding.SpaceSetupActivity
import net.opendasharchive.openarchive.util.extensions.Position
import net.opendasharchive.openarchive.util.extensions.hide
import net.opendasharchive.openarchive.util.extensions.setDrawable
import net.opendasharchive.openarchive.util.extensions.tint

class AddFolderActivity : BaseActivity() {

    companion object {
        const val EXTRA_FOLDER_ID = "folder_id"
        const val EXTRA_FOLDER_NAME = "folder_name"
    }

    private lateinit var mBinding: ActivityAddFolderBinding
    private lateinit var mResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                setResult(RESULT_OK, it.data)
                finish()
            }
            else {
                val name = it.data?.getStringExtra(EXTRA_FOLDER_NAME)

                if (!name.isNullOrBlank()) {
                    val i = Intent(this, CreateNewFolderActivity::class.java)
                    i.putExtra(EXTRA_FOLDER_NAME, name)

                    mResultLauncher.launch(i)
                }
            }
        }

        mBinding = ActivityAddFolderBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.newFolder.setOnClickListener {
            setFolder(false)
        }

        mBinding.browseFolders.setOnClickListener {
            setFolder(true)
        }

        val arrow = ContextCompat.getDrawable(this, R.drawable.ic_arrow_right)
        arrow?.tint(ContextCompat.getColor(this, R.color.colorPrimary))

        mBinding.newFolderText.setDrawable(arrow, Position.End, tint = false)
        mBinding.browseFoldersText.setDrawable(arrow, Position.End, tint = false)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // We cannot browse the Internet Archive. Directly forward to creating a project,
        // as it doesn't make sense to show a one-option menu.
        if (Space.current?.tType == Space.Type.INTERNET_ARCHIVE) {
            mBinding.browseFolders.hide()

            finish()
            setFolder(false)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setFolder(browse: Boolean) {
        if (Space.current == null) {
            finish()
            startActivity(Intent(this, SpaceSetupActivity::class.java))

            return
        }

        mResultLauncher.launch(Intent(this,
            if (browse) BrowseFoldersActivity::class.java else CreateNewFolderActivity::class.java))
    }
}