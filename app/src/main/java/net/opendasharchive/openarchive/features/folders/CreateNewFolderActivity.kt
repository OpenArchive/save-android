package net.opendasharchive.openarchive.features.folders

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityCreateNewFolderBinding
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.core.BaseActivity
import net.opendasharchive.openarchive.features.settings.CcSelector
import net.opendasharchive.openarchive.services.webdav.ReadyToAuthTextWatcher
import net.opendasharchive.openarchive.util.Utility.showMaterialPrompt
import net.opendasharchive.openarchive.util.Utility.showMaterialWarning
import net.opendasharchive.openarchive.util.extensions.hide
import java.util.Date

class CreateNewFolderActivity : BaseActivity() {

    companion object {
        private const val SPECIAL_CHARS = ".*[\\\\/*\\s]"
    }

    private lateinit var binding: ActivityCreateNewFolderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreateNewFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create New Folder" // getString(R.string.new_folder)

        binding.newFolderName.requestFocus()

        binding.newFolderName.setText(intent.getStringExtra(AddFolderActivity.EXTRA_FOLDER_NAME))

        binding.newFolderName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                store()
            }

            false
        }

        binding.newFolderName.addTextChangedListener(object : ReadyToAuthTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                enableIfReady()
            }
        })

        if (Folder.current?.backend?.license == null) {
            CcSelector.init(binding.cc, license = "https://creativecommons.org/licenses/by-sa/4.0")
        }
        else {
            binding.cc.root.hide()
        }

        binding.createFolderButton.setOnClickListener {
            store()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
//            R.id.action_done -> {
//                store()
//                return true
//            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun enableIfReady() {
        val isComplete = !binding.newFolderName.text.isNullOrEmpty()

        binding.createFolderButton.isEnabled = isComplete
    }

    private fun store() {
        val name = binding.newFolderName.text.toString()

        if (name.isBlank()) return

        if (name.matches(SPECIAL_CHARS.toRegex())) {
            showMaterialWarning(this, "Oops", getString(R.string.please_do_not_include_special_characters_in_the_name), "Ok")
            return
        }

        val backend = Folder.current?.backend ?: return

        if (backend.hasFolder(name)) {
            showMaterialWarning(this, "Oops", getString(R.string.folder_name_already_exists), "Ok")
            return
        }

        val license = backend.license ?: CcSelector.get(binding.cc)

        val folder = Folder(name, Date(), backend, licenseUrl = license)
        folder.save()

        showMaterialPrompt(
            this,
            "Folder created",
            "Would you like to make this your current folder?",
            "Yes", "No") { affirm ->

            if (affirm) {
                Folder.current = folder
            }

            signalSuccess(folder.id)
        }
    }

    private fun signalSuccess(folderId: Long) {
        val i = Intent()
        i.putExtra(AddFolderActivity.EXTRA_FOLDER_ID, folderId)

        setResult(RESULT_OK, i)
        finish()
    }
}
