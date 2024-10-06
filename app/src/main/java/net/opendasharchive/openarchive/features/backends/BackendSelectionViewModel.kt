package net.opendasharchive.openarchive.features.backends

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.Backend
import timber.log.Timber

sealed class GroupedBackend {
    data class Header(val title: String) : GroupedBackend()
    data class Item(val backend: Backend) : GroupedBackend()
}

data class GroupedBackends(
    val headers: List<GroupedBackend.Header>,
    val itemsMap: Map<GroupedBackend.Header, List<GroupedBackend.Item>>) {

    fun toFlattenedList(): List<GroupedBackend> {
        return headers.flatMap { header ->
            listOf(header) + (itemsMap[header] ?: emptyList())
        }
    }
}

class BackendSelectionViewModel : ViewModel() {
    private val _backends = MutableLiveData<GroupedBackends>()
    val backends: LiveData<GroupedBackends> = _backends

    init {
        loadGroupedBackends()
    }

    private fun loadGroupedBackends() {
        viewModelScope.launch {
            try {
                val backends = mutableListOf(
                    "Create New Connection" to Backend.ALL_BACKENDS)

                val connectedBackends = Backend.getAll().toList()

                if (connectedBackends.isNotEmpty()) {
                    backends.add(0, "Already Connected" to Backend.getAll().toList())
                }
                _backends.value = processBackends(backends)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun processBackends(group: List<Pair<String, List<Backend>>>): GroupedBackends {
        val headers = mutableListOf<GroupedBackend.Header>()
        val itemsMap = mutableMapOf<GroupedBackend.Header, MutableList<GroupedBackend.Item>>()

        group.forEach { (title, backends) ->
            val header = GroupedBackend.Header(title)
            headers.add(header)

            itemsMap[header] = mutableListOf()

            backends.forEach { backend ->
                Timber.d("Adding backend item $backend")
                itemsMap[header]?.add(GroupedBackend.Item(backend))
            }
        }

        return GroupedBackends(headers, itemsMap)
    }

    fun refresh() {
        loadGroupedBackends()
    }
}