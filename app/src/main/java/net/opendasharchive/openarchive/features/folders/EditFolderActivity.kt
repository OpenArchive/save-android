package net.opendasharchive.openarchive.features.folders

import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityEditFolderBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.settings.CcSelector
import net.opendasharchive.openarchive.util.AlertHelper
import net.opendasharchive.openarchive.extensions.Position
import net.opendasharchive.openarchive.extensions.setDrawable

class EditFolderActivity : BaseActivity() {

    companion object {
        const val EXTRA_CURRENT_FOLDER_ID = "archive_extra_current_folder_id"
    }

    private lateinit var mFolder: Folder
    private lateinit var mBinding: ActivityEditFolderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val folder = Folder.getById(intent.getLongExtra(EXTRA_CURRENT_FOLDER_ID, -1L))
            ?: return finish()

        mFolder = folder

        mBinding = ActivityEditFolderBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mBinding.folderName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newName = mBinding.folderName.text.toString()

                if (newName.isNotBlank()) {
                    mFolder.name = newName
                    mFolder.save()

                    supportActionBar?.title = newName
                    mBinding.folderName.hint = newName
                }
            }

            false
        }

        mBinding.btRemove.setDrawable(R.drawable.ic_delete, Position.Start, 0.5)
        mBinding.btRemove.setOnClickListener {
            removeProject()
        }

        mBinding.btArchive.setOnClickListener {
            archiveProject()
        }

        CcSelector.init(mBinding.cc, null) {
            mFolder.license = it
            mFolder.save()
        }

        updateUi()
    }

    private fun removeProject() {
        AlertHelper.show(this, R.string.action_remove_project, R.string.remove_from_app, buttons = listOf(
            AlertHelper.positiveButton(R.string.remove) { _, _ ->
                mFolder.delete()

                finish()
            },
            AlertHelper.negativeButton()))
    }

    private fun archiveProject() {
        mFolder.isArchived = !mFolder.isArchived
        mFolder.save()

        updateUi()
    }

    private fun updateUi() {
        supportActionBar?.title = mFolder.name

        mBinding.folderName.isEnabled = !mFolder.isArchived
        mBinding.folderName.hint = mFolder.name
        mBinding.folderName.setText(mFolder.name)

        mBinding.btArchive.setText(if (mFolder.isArchived)
            R.string.action_unarchive_project else
            R.string.action_archive_project)

        val global = mFolder.backend?.license != null

        if (global) {
            mBinding.cc.tvCc.setText(R.string.set_the_same_creative_commons_license_for_all_folders_on_this_server)
        }

        CcSelector.set(mBinding.cc, mFolder.license, !mFolder.isArchived && !global)
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
}