package net.opendasharchive.openarchive.features.main.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.opendasharchive.openarchive.db.ICollectionRepository
import net.opendasharchive.openarchive.db.IFolderRepository
import net.opendasharchive.openarchive.db.Media
import net.opendasharchive.openarchive.extensions.isYesterday
import net.opendasharchive.openarchive.extensions.toggle
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class GridSectionViewModel(
    private val folderRepository: IFolderRepository,
    private val collectionRepository: ICollectionRepository
) : ViewModel() {
    private val _items = MutableStateFlow<List<GridSectionItem>>(emptyList())
    val items: StateFlow<List<GridSectionItem>> = _items.asStateFlow()

    private val _selectedItems = MutableStateFlow<Set<Int>>(emptySet())
    val selectedItems: StateFlow<Set<Int>> = _selectedItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val dateTimeFormatter = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun addNewMedia(media: Media) {
        viewModelScope.launch {
            _items.update { currentItems ->
                val newThumbnail = GridSectionItem.Thumbnail(media)
                val secondHeaderIndex = findSecondHeaderIndex(currentItems)

                if (secondHeaderIndex == -1) {
                    // If there's no second header, add to the end of the list
                    currentItems + newThumbnail
                } else {
                    // Insert the new thumbnail just before the second header
                    currentItems.toMutableList().apply {
                        add(secondHeaderIndex, newThumbnail)
                    }
                }
            }
        }
    }

    private fun findSecondHeaderIndex(items: List<GridSectionItem>): Int {
        val firstHeaderIndex = items.indexOfFirst { it is GridSectionItem.Header }
        if (firstHeaderIndex == -1) return -1 // No headers found

        return items.drop(firstHeaderIndex + 1).indexOfFirst { it is GridSectionItem.Header }.let { secondIndex ->
            if (secondIndex == -1) -1 else firstHeaderIndex + 1 + secondIndex
        }
    }

    fun loadItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newItems = withContext(Dispatchers.Default) {
                    val folder = folderRepository.getCurrentFolder() ?: return@withContext emptyList()

                    collectionRepository.getCollectionsByFolder(folder.id)
                        .sortedBy { it.uploadDate }
                        .flatMap { collection ->
                            buildList {
                                add(GridSectionItem.Header(
                                    formatDate(collection.uploadDate),
                                    collection.media.size
                                ))
                                addAll(collection.media.map { GridSectionItem.Thumbnail(it) })
                            }
                        }
                }
                updateItemsIfChanged(newItems)
            } catch (e: Exception) {
                Timber.e(e, "Error loading items")
            } finally {
                _isLoading.value = false
            }
        }
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

    fun toggleItemSelection(position: Int) {
        _selectedItems.value = _selectedItems.value.toMutableSet().apply {
            toggle(position)
        }
    }

    private fun updateItemsIfChanged(newItems: List<GridSectionItem>) {
        if (newItems != _items.value) {
            _items.value = newItems
        }
    }
}