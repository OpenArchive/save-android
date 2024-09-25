package net.opendasharchive.openarchive.features.main.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.db.Collection
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.extensions.isYesterday
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GridSectionViewModel : ViewModel() {
    private val _items = MutableLiveData<List<GridSectionItem>>()
    val items: LiveData<List<GridSectionItem>> = _items

    private val dateTimeFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun loadItems() {
        val folder = Folder.current ?: return

        val items = mutableListOf<GridSectionItem>()

        val collections = Collection.getByFolder(folder.id).associateBy { it.id }.toMutableMap()

        Timber.d("collections = $collections")

        collections.forEach { (id, collection) ->
            items.add(GridSectionItem.Header(formatDate(collection.uploadDate)))

            val media = collection.media

            media.forEach { mediaItem ->
                items.add(GridSectionItem.Image(mediaItem.originalFilePath))
            }
        }

        _items.value = items
    }

    private fun formatDate(date: Date?): String {
        if (date == null) {
            return "Uploading..."
        }

        if (date.isYesterday()) {
            return "Today at " + timeFormatter.format(date)
        }

        if (date.isYesterday()) {
            return "Yesterday at " + timeFormatter.format(date)
        }

        return dateTimeFormatter.format(date)
    }
}