package net.opendasharchive.openarchive.features.main.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.db.Collection
import net.opendasharchive.openarchive.db.Folder
import timber.log.Timber

class GridSectionViewModel : ViewModel() {
    private val _items = MutableLiveData<List<GridSectionItem>>()
    val items: LiveData<List<GridSectionItem>> = _items

    fun loadItems() {
//        val newItems = listOf(
//            GridSectionItem.Header("Section 1"),
//            GridSectionItem.Image("https://placedog.net/400x200"),
//            GridSectionItem.Image("https://placedog.net/400x400"),
//            GridSectionItem.Image("https://placedog.net/400x400"),
//            GridSectionItem.Header("Section 2"),
//            GridSectionItem.Image("https://placedog.net/200x200"),
//            GridSectionItem.Image("https://placedog.net/100x100"),
//            GridSectionItem.Image("https://placedog.net/600x600")
//        )
//        _items.value = newItems

        reload()
    }

    private fun reload() {
        val folder = Folder.current ?: return

        var items = mutableListOf<GridSectionItem>()

        val collections = Collection.getByFolder(folder.id).associateBy { it.id }.toMutableMap()

        Timber.d("collections = $collections")

        collections.forEach { (id, collection) ->
            items.add(GridSectionItem.Header("Section 1"))

            val media = collection.media

            media.forEach { mediaItem ->
                items.add(GridSectionItem.Image(mediaItem.originalFilePath))
            }
        }

        _items.value = items
    }
}