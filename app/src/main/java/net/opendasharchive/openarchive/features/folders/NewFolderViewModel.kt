package net.opendasharchive.openarchive.features.folders

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import net.opendasharchive.openarchive.db.Backend
import net.opendasharchive.openarchive.db.Folder
import net.opendasharchive.openarchive.features.core.MultiObserverEvent
import net.opendasharchive.openarchive.features.core.MultiObserverLiveData

sealed class WizardNavigationAction {
    data object BackendSelected: WizardNavigationAction()
    data object FolderCreated : WizardNavigationAction()
    data object FolderMetadataCreated : WizardNavigationAction()
    data object UserAuthenticated : WizardNavigationAction()
}

class NewFolderViewModel : ViewModel() {
    lateinit var folder: Folder
    lateinit var backend: Backend

    private val _navigationEvent = MultiObserverLiveData<WizardNavigationAction>()
    val navigationEvent: LiveData<MultiObserverEvent<WizardNavigationAction>> = _navigationEvent

    fun observeNavigation(owner: LifecycleOwner, observer: Observer<WizardNavigationAction>) {
        _navigationEvent.addObserver(owner, observer)
    }

    fun triggerNavigation(action: WizardNavigationAction) {
        _navigationEvent.setEvent(action)
    }

//    private val _folder = MutableLiveData<Folder?>()
//    var folder: LiveData<Folder?> = _folder
//
//    private val _navigationEvent = MutableLiveData<OAEvent<WizardNavigationAction>>()
//    val navigationEvent: LiveData<OAEvent<WizardNavigationAction>> = _navigationEvent
//
//    lateinit var backend: Backend
//
//    fun setActionCompleted(action: WizardNavigationAction) {
//        _navigationEvent.postValue(OAEvent(action))
//    }
//
//    fun setFolder(folder: Folder?) {
//        _folder.postValue(folder)
//    }
}