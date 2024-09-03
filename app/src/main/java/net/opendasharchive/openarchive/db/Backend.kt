package net.opendasharchive.openarchive.db

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.amulyakhare.textdrawable.TextDrawable
import com.github.abdularis.civ.AvatarImageView
import com.orm.SugarRecord
import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.features.backends.BackendSetupActivity
import net.opendasharchive.openarchive.services.gdrive.GDriveConduit
import net.opendasharchive.openarchive.services.internetarchive.IaConduit
import net.opendasharchive.openarchive.services.webdav.WebDavConduit
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import timber.log.Timber
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class BackendResult() {
    Cancelled, Created, Deleted, Modified
}

data class Backend(
    var type: Long = 0,
    var name: String = "",
    var username: String = "",
    var displayname: String = "",
    var password: String = "",
    var host: String = "",
    var metaData: String = "",
    var lastSyncDate: Date? = null,
    private var licenseUrl: String? = null
) : SugarRecord() {

    constructor(type: Type) : this() {
        tType = type

        when (type) {
            Type.WEBDAV -> {
                name = WebDavConduit.NAME
            }
            Type.INTERNET_ARCHIVE -> {
                name = IaConduit.NAME
                host = IaConduit.ARCHIVE_API_ENDPOINT
            }
            Type.GDRIVE -> {
                name = GDriveConduit.NAME
            }
            Type.VEILID -> {
                name = "Veilid"
            }
        }
    }

    enum class Type(val id: Long, val friendlyName: String) {
        WEBDAV(0, WebDavConduit.NAME),
        INTERNET_ARCHIVE(1, IaConduit.NAME),
        GDRIVE(4, GDriveConduit.NAME),
        VEILID(5, "Veilid");

        companion object {
            operator fun invoke(raw: Long): Type? = entries.firstOrNull { it.id == raw }
        }
    }

    companion object {
        val ALL_BACKENDS = listOf(
            Backend(Backend.Type.INTERNET_ARCHIVE),
            Backend(Backend.Type.WEBDAV),
            Backend(Backend.Type.GDRIVE),
        )

        fun getAll(): List<Backend> {
            return findAll(Backend::class.java).asSequence().toList()
        }

        fun get(type: Type, host: String? = null, username: String? = null): List<Backend> {
            var whereClause = "type = ?"
            val whereArgs = mutableListOf(type.id.toString())

            if (!host.isNullOrEmpty()) {
                whereClause = "$whereClause AND host = ?"
                whereArgs.add(host)
            }

            if (!username.isNullOrEmpty()) {
                whereClause = "$whereClause AND username = ?"
                whereArgs.add(username)
            }

            return find(Backend::class.java, whereClause, whereArgs.toTypedArray(), null, null, null)
        }

        fun has(type: Type, host: String? = null, username: String? = null): Boolean {
            return get(type, host, username).isNotEmpty()
        }

//        var current: Backend?
//            get() = getById(Prefs.currentBackendId)
//            set(value) {
//                Prefs.currentBackendId = value?.id ?: -1
//            }

        fun getById(id: Long): Backend? {
            return findById(Backend::class.java, id)
        }

        fun getFolderById(id: Long): Folder? {
            return findById(Folder::class.java, id)
        }

        fun navigate(activity: AppCompatActivity) {
            if (getAll().isNotEmpty()) {
                activity.finish()
            }
            else {
                activity.finishAffinity()
                activity.startActivity(Intent(activity, BackendSetupActivity::class.java))
            }
        }
    }

    val friendlyName: String
        get() {
            if (name.isNotBlank()) {
                return name
            }

            return hostUrl?.host ?: name
        }

    val initial: String
        get() = (friendlyName.firstOrNull() ?: 'X').uppercase(Locale.getDefault())

    val hostUrl: HttpUrl?
        get() = host.toHttpUrlOrNull()

    var tType: Type?
        get() = Type.entries.firstOrNull { it.id == type }
        set(value) {
            type = (value ?: Type.WEBDAV).id
        }

    var license: String?
        get() = this.licenseUrl
        set(value) {
            licenseUrl = value

            for (folder in folders) {
                folder.licenseUrl = licenseUrl
                folder.save()
            }
        }

    fun exists(): Boolean {
        try {
            val items = find(
                Backend::class.java,
                "type = ? AND displayname = ?", type.toString(), displayname)

            return items.isNotEmpty()
        } catch (e: Exception) {
            Timber.d("Error = ${e.localizedMessage}")
            return false
        }
    }

    val folders: List<Folder>
        get() = find(Folder::class.java, "backend = ? AND NOT archived", arrayOf(id.toString()), null, "id DESC", null)

    val archivedFolders: List<Folder>
        get() = find(Folder::class.java, "backend = ? AND archived", arrayOf(id.toString()), null, "id DESC", null)

    fun hasFolder(description: String?): Boolean {
        if (description == null) { return false }
        // Cannot use `count` from Kotlin due to strange <T> in method signature.
        return find(Folder::class.java, "backend = ? AND description = ?", id.toString(), description).size > 0
    }

    fun getAllForType(type: Type): List<Backend> {
        return get(type, null, null)
    }

    fun getAvatar(context: Context): Drawable? {
        val color = ContextCompat.getColor(context, R.color.colorOnBackground)

        return when (tType) {
            Type.WEBDAV -> ContextCompat.getDrawable(context, R.drawable.ic_private_server)

            Type.INTERNET_ARCHIVE -> ContextCompat.getDrawable(context, R.drawable.ic_internet_archive)

            Type.GDRIVE -> ContextCompat.getDrawable(context, R.drawable.logo_drive_2020q4_color_2x_web_64dp)

            else -> TextDrawable.builder().buildRound(initial, color)
        }
    }

    fun setAvatar(view: ImageView) {
        when (tType) {
            Type.INTERNET_ARCHIVE -> {
                if (view is AvatarImageView) {
                    view.state = AvatarImageView.SHOW_IMAGE
                }

                view.setImageDrawable(getAvatar(view.context))
            }

            else -> {
                if (view is AvatarImageView) {
                    view.state = AvatarImageView.SHOW_INITIAL
                    view.setText(initial)
                    view.avatarBackgroundColor = ContextCompat.getColor(view.context, R.color.colorPrimary)
                }
                else {
                    view.setImageDrawable(getAvatar(view.context))
                }
            }
        }
    }

    fun shouldSync(): Boolean {
        if (lastSyncDate == null) {
            return true
        }

        val duration = Date().time - lastSyncDate!!.time
        val days = TimeUnit.MILLISECONDS.toDays(duration)

        Timber.d("Days = $days")

        if (days > 7) {
            Timber.d("Sync is required")
            return true
        }

        Timber.d("Sync is not required")
        return false
    }

    override fun delete(): Boolean {
        folders.forEach {
            it.delete()
        }

        return super.delete()
    }
}