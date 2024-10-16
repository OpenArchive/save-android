package net.opendasharchive.openarchive.db

import com.orm.SugarRecord
import net.opendasharchive.openarchive.util.Prefs
import net.opendasharchive.openarchive.util.Prefs.licenseUrl
import timber.log.Timber
import java.util.Date

data class Folder(
    var name: String? = null,
    var created: Date = Date(),
    var backend: Backend? = null,
    private var archived: Boolean = false,
    private var openCollectionId: Long = -1,
    var license: String? = null
) : SugarRecord() {

    fun doesNotExist(): Boolean {
        return !exists()
    }

    fun exists(): Boolean {
        if (backend?.id == null) {
            return false
        }

        try {
            val items = find(
                Folder::class.java,
                "backend = ? AND name = ?", backend?.id.toString(), name
            )

            return items.isNotEmpty()
        } catch (e: Exception) {
            Timber.d("Error = ${e.localizedMessage}")
            return false
        }
    }

    fun hasBackend(): Boolean {
        return (backend != null)
    }

    companion object {
        var current: Folder?
            get() = getById(Prefs.currentFolderId)
            set(value) {
                Prefs.currentFolderId = value?.id ?: -1
            }

        fun getById(folderId: Long?): Folder? {
            @Suppress("NAME_SHADOWING")
            val folderId = folderId ?: return null

            return findById(Folder::class.java, folderId)
        }

        fun getLocalFoldersForBackend(backend: Backend?): List<Folder> {
            // It's possible that this Folder's backend hasn't been
            // created yet.
            //
            if (backend == null || backend.id == null) {
                return listOf()
            }

            return find(
                Folder::class.java,
                "backend = ?", backend.id.toString()
            )
        }
    }

    var isArchived: Boolean
        get() = archived
        set(value) {
              archived = value

            // When the space has a license, that needs to be applied when de-archived.
            // Otherwise the wrong license setting might get transmitted to the server.
            if (!archived) {
                val sl = backend?.license

                if (!sl.isNullOrBlank()) licenseUrl = sl
            }
        }

    val isCurrent
        get() = (id == current?.id)

    val collections: List<Collection>
        get() = find(Collection::class.java, "folder_id = ?", id.toString())

    val openCollection: Collection
        get() {
            var collection = findById(Collection::class.java, openCollectionId)

            if (collection == null || collection.uploadDate != null) {
                collection = Collection(folderId = id)
                collection.save()

                openCollectionId = collection.id
                save()
            }

            return collection
        }

    override fun delete(): Boolean {
        collections.forEach {
            it.delete()
        }

        return super.delete()
    }
}