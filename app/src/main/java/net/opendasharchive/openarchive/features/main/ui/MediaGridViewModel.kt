package net.opendasharchive.openarchive.features.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.Collection
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.db.ICollectionRepository
import net.opendasharchive.openarchive.db.IFolderRepository
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.extensions.friendlyString
import net.opendasharchive.openarchive.upload.MediaWithState
import timber.log.Timber

class MediaGridViewModel(
    private val folderRepository: IFolderRepository,
    private val collectionRepository: ICollectionRepository
) : ViewModel() {
    private val _items = MutableStateFlow<List<GridSectionItem>>(emptyList())
    val items: StateFlow<List<GridSectionItem>> = _items.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<Int>>(emptySet())
    val selectedItems: StateFlow<Set<Int>> = _selectedItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun addNewMedia(media: Media) {
        viewModelScope.launch {
            loadItems()
        }
    }

    fun loadItems() {
        val folder = Folder.current ?: return

        val items = mutableListOf<GridSectionItem>()

        val collections = Collection.getByFolder(folder.id).associateBy { it.id }.toMutableMap()

        Timber.d("collections = $collections")

        collections.forEach { (_, collection) ->
            items.add(GridSectionItem.Header(
                collection.uploadDate.friendlyString(),
                collection.media.size))

            collection.media.forEach { media ->
                items.add(GridSectionItem.Thumbnail(
                    MediaWithState(media, WorkInfo.State.SUCCEEDED)
                ))
            }
        }

        _items.value = items
    }

//    fun toggleItemSelection(position: Int) {
//        _selectedItems.value = _selectedItems.value.toMutableSet().apply {
//            val folder = Folder.current ?: return@withContext emptyList()
//
//            Collection.getByFolder(folder.id)
//                .sortedBy { it.uploadDate }
//                .flatMap { collection ->
//                    buildList {
//                        add(GridSectionItem.Header(
//                            formatDate(collection.uploadDate),
//                            collection.media.size
//                        ))
//                        addAll(collection.media.map { GridSectionItem.Thumbnail(MediaWithState(it, null)) })
//                    }
//                }
//
//            sectionedItems
//        }
//    }

    private fun updateItemsIfChanged(newItems: List<GridSectionItem>) {
        if (newItems != _items.value) {
            _items.value = newItems
        }
    }
}