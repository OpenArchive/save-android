package net.opendasharchive.openarchive.features.main

import net.opendasharchive.openarchive.R
import net.opendasharchive.openarchive.databinding.ViewSectionBinding
import net.opendasharchive.openarchive.db.Collection
import net.opendasharchive.openarchive.db.Media
import java.text.DateFormat
import java.text.NumberFormat

data class SectionViewHolder(private val binding: ViewSectionBinding) {

    companion object {
        private val numberFormatter
            get() = NumberFormat.getIntegerInstance()

        private val dateFormatter
            get() = DateFormat.getDateTimeInstance()
    }

    val root
        get() = binding.root

    private val timestamp
        get() = binding.timestamp

    private val count
        get() = binding.count

    val recyclerView
        get() = binding.recyclerView

    fun setHeader(collection: Collection, media: List<Media>) {
        if (media.any { it.isUploading }) {
            timestamp.setText(R.string.uploading)

            val uploaded = media.filter { it.status == Media.Status.Uploaded }.size

            count.text = count.context.getString(R.string.counter, uploaded, media.size)

            return
        }

        count.text = numberFormatter.format(media.size)

        val uploadDate = collection.uploadDate

        timestamp.text = if (uploadDate != null) dateFormatter.format(uploadDate) else "Ready to upload"
    }
}