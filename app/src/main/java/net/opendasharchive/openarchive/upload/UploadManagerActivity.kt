package net.opendasharchive.openarchive.upload

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ActivityUploadManagerBinding
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.features.core.BaseActivity

class UploadManagerActivity : BaseActivity() {

    private lateinit var mBinding: ActivityUploadManagerBinding
    var mFrag: UploadManagerFragment? = null
    private var mMenuEdit: MenuItem? = null

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        private val handler = Handler(Looper.getMainLooper())

        override fun onReceive(context: Context, intent: Intent) {
            val action = BroadcastManager.getAction(intent)
            val mediaId = action?.mediaId ?: return

            if (mediaId > -1) {
                val media = Media.get(mediaId)

                if (action == BroadcastManager.Action.Delete || media?.status == Media.Status.Uploaded) {
                    handler.post { mFrag?.removeItem(mediaId) }
                }
                else {
                    handler.post { mFrag?.updateItem(mediaId) }
                }

//                if (media?.sStatus == Media.Status.Error) {
//                    CleanInsightsManager.getConsent(this@UploadManagerActivity) {
//                        // TODO: Record metadata. See iOS implementation.
//                        CleanInsightsManager.measureEvent("upload", "upload_failed")
//                    }
//                }
            }

            handler.post {
                updateTitle()
            }
        }
    }

    private var mEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityUploadManagerBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mFrag = supportFragmentManager.findFragmentById(R.id.fragUploadManager) as? UploadManagerFragment
    }

    override fun onResume() {
        super.onResume()
        mFrag?.refresh()

        BroadcastManager.register(this, mMessageReceiver)

        updateTitle()
    }

    override fun onPause() {
        super.onPause()

        BroadcastManager.unregister(this, mMessageReceiver)
    }

    private fun toggleEditMode() {
        mEditMode = !mEditMode
        mFrag?.setEditMode(mEditMode)
        mFrag?.refresh()

        if (mEditMode) {
            mMenuEdit?.setTitle(R.string.menu_done)

            UploadService.stopUploadService(this)
        }
        else {
            mMenuEdit?.setTitle(R.string.edit)

            UploadService.startUploadService(this)
        }

        updateTitle()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_upload, menu)
        mMenuEdit = menu.findItem(R.id.menu_edit)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_edit -> {
                toggleEditMode()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        // If we're still in edit mode, restart the upload service when the user leaves.
        if (mEditMode) {
            UploadService.startUploadService(this)
        }

        super.finish()
    }

    private fun updateTitle() {
        if (mEditMode) {
            supportActionBar?.title = getString(R.string.edit_media)
            supportActionBar?.subtitle = getString(R.string.uploading_is_paused)
        }
        else {
            val count = mFrag?.getUploadingCounter() ?: 0

            supportActionBar?.title = if (count < 1) {
                getString(R.string.uploads)
            } else {
                getString(R.string.uploading_left, count)
            }

            supportActionBar?.subtitle = null
        }
    }
}