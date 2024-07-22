package net.opendasharchive.openarchive.db

import android.net.Uri
import androidx.core.net.toFile
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.orm.SugarRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

data class Media(
    var originalFilePath: String = "",

    @Expose
    @SerializedName("contentType")
    var mimeType: String = "",

    @Expose
    @SerializedName("dateCreated")
    var createDate: Date? = null,

    var updateDate: Date? = null,
    var uploadDate: Date? = null,
    var serverUrl: String = "",

    @Expose
    @SerializedName("originalFileName")
    var title: String = "",

    @Expose
    var description: String = "",

    @Expose
    var author: String = "",

    @Expose
    var location: String = "",

    @Expose
    var tags: String = "",

    @Expose
    @SerializedName("usage")
    var licenseUrl: String? = null,

    var mediaHash: ByteArray = byteArrayOf(),

    @Expose
    @SerializedName(value = "hash")
    var mediaHashString: String = "",

    var status: Status = Status.Unknown,
    var statusMessage: String = "",
    var projectId: Long = 0,
    var collectionId: Long = 0,

    @Expose
    var contentLength: Long = 0,

    var progress: Long = 0,
    var flag: Boolean = false,
    var priority: Int = 0,
    var selected: Boolean = false
) : SugarRecord() {

    enum class Status(val id: Int) {
        Unknown(-1),
        New(0),
        Local(1),
        Queued(2),
        Uploading(4),
        Uploaded(5),
        Error(9),
    }

    companion object {
        const val ORDER_PRIORITY = "priority DESC"
        const val ORDER_CREATED = "create_date DESC"

        fun getAll(order: String? = null): List<Media> {
            return findAll(Media::class.java).asSequence().toList()
        }

        fun getByStatus(statuses: List<Status>, order: String? = null): List<Media> {
            val statusStatement = statuses.joinToString(" OR ") { "status = ?" }
            val statusString = statuses.map { it.toString() }.toTypedArray()

            return find(Media::class.java,
                statusStatement, statusString,null, order, null)
        }

        fun get(mediaId: Long?): Media? {
            @Suppress("NAME_SHADOWING")
            val mediaId = mediaId ?: return null

            return findById(Media::class.java, mediaId)
        }
    }

    val formattedCreateDate: String
        get() {
            return createDate?.let {
                SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(it)
            } ?: ""
        }

//    var sStatus: Status
//        get() = status // Status.values().firstOrNull { it.id == status } ?: Status.New
//        set(value) {
//            status = value
//        }

    val fileUri: Uri
        get() = Uri.parse(originalFilePath)

    val file: File
        get() = fileUri.toFile()

    val collection: Collection?
        get() = findById(Collection::class.java, collectionId)

    val project: Project?
        get() = findById(Project::class.java, projectId)

    val space: Space?
        get() = project?.space

    val isUploading
        get() =  status == Status.Queued
                || status == Status.Uploading
                || status == Status.Error

    var tagSet: MutableSet<String>
        get() = tags.split("\\p{Punct}|\\p{Blank}+".toRegex()).map { it.trim() }.toMutableSet()
        set(value) {
            tags = value.joinToString(";")
        }
}
