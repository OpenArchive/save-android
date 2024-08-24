package net.opendasharchive.openarchive.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orm.SugarRecord
import net.opendasharchive.openarchive.util.Prefs
import java.util.Date

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)

data class Folder(
    var description: String? = null,
    var created: Date? = null,
    var backendId: Long? = null,
    private var archived: Boolean = false,
    private var openCollectionId: Long = -1,
    var licenseUrl: String? = null
) : SugarRecord() {

    fun exists(): Boolean {
        return findWithQuery(
            Folder::class.java,
            "backendId = ? and description = ?",
            backendId.toString(), description).isNotEmpty()
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

    val isUploading
        get() = collections.any { it.isUploading }

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

    val backend: Backend?
        get() = findById(Backend::class.java, backendId)

}