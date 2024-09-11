package net.opendasharchive.openarchive.features.core

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class MultiObserverEvent<T>(private val content: T) {
    private val consumed = AtomicBoolean(false)

    fun getContentIfNotConsumed(): T? {
        return if (consumed.compareAndSet(false, true)) content else null
    }

    fun peekContent(): T = content
}

class MultiObserverLiveData<T> : MutableLiveData<MultiObserverEvent<T>>() {
    private val observers = mutableMapOf<Observer<in T>, Observer<MultiObserverEvent<T>>>()

    fun addObserver(owner: LifecycleOwner, observer: Observer<in T>) {
        val wrappedObserver = Observer<MultiObserverEvent<T>> { event ->
            event.getContentIfNotConsumed()?.let { content ->
                observer.onChanged(content)
            }
        }
        observers[observer] = wrappedObserver
        super.observe(owner, wrappedObserver)
    }

    fun removeMultiObserver(observer: Observer<in T>) {
        observers.remove(observer)?.let { wrappedObserver ->
            super.removeObserver(wrappedObserver)
        }
    }

    fun postEvent(value: T) {
        postValue(MultiObserverEvent(value))
    }

//    fun setEvent(value: T) {
//        setValue(MultiObserverEvent(value))
//    }
}