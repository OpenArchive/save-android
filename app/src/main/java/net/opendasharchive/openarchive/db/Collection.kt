package net.opendasharchive.openarchive.db

import com.orm.SugarRecord
import java.util.*

data class Collection(
    var folderId: Long? = null,
    var uploadDate: Date? = null,
    var serverUrl: String? = null
) : SugarRecord() {

    companion object {

        fun getByFolder(folderId: Long): List<Collection> {
            return find(Collection::class.java, "folder_id = ?", arrayOf(folderId.toString()),
                null, "id ASC", null)
        }

        fun get(collectionId: Long?): Collection? {
            @Suppress("NAME_SHADOWING")
            val collectionId = collectionId ?: return null

            return findById(Collection::class.java, collectionId)
        }
    }

    val media: MutableList<Media>
        get() = find(Media::class.java, "collection_id = ?", arrayOf(id.toString()), null, "status, id DESC", null)

    val size: Long
        get() = count<Media>(Media::class.java, "collection_id = ?", arrayOf(id.toString()))

    val isUploading
        get() = media.firstOrNull { it.isUploading } != null


    override fun delete(): Boolean {
        media.forEach {
            it.delete()
        }

        return super.delete()
    }
}