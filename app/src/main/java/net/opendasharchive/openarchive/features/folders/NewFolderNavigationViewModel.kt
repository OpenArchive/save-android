package net.opendasharchive.openarchive.features.folders

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.features.core.MultiObserverEvent
import net.opendasharchive.openarchive.features.core.MultiObserverLiveData

sealed class NewFolderNavigationAction {
    data object FolderCreated : NewFolderNavigationAction()
    data object BackendMetadataCreated : NewFolderNavigationAction()
    data object UserAuthenticated : NewFolderNavigationAction()
}

class NewFolderNavigationViewModel : ViewModel() {
    private val _navigationEvent = MultiObserverLiveData<NewFolderNavigationAction>()
    val navigationEvent: LiveData<MultiObserverEvent<NewFolderNavigationAction>> = _navigationEvent

    fun observeNavigation(owner: LifecycleOwner, observer: Observer<NewFolderNavigationAction>) {
        _navigationEvent.addObserver(owner, observer)
    }

    fun triggerNavigation(action: NewFolderNavigationAction) {
        _navigationEvent.postEvent(action)
    }
}