package com.rockspin.rxredux.android

import androidx.lifecycle.LifecycleOwner
import com.rockspin.rxredux.core.Action
import com.rockspin.rxredux.core.Event
import com.rockspin.rxredux.core.State
import com.rockspin.rxredux.core.Store
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

/**
 * Connect to an observable for state updates. Will not emit duplicate events.
 */
fun <T> Observable<T>.connect(scope: ScopeProvider, stateHandler: (T) -> Unit = {}): Disposable =
        this
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope)
                .subscribe(stateHandler)

/**
 * Connect to an observable for state updates. Will emit duplicate events.
 */
fun <T> Observable<T>.connectEvery(scope: ScopeProvider, stateHandler: (T) -> Unit = {}): Disposable =
        this
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scope)
                .subscribe(stateHandler)

/**
 * Connect to a store, will be disconnected using autoDispose.
 */
fun <VS : State> Store<VS>.connect(lifecycleOwner: LifecycleOwner, stateHandler: (VS) -> Unit = {}): Disposable =
        updates.connect(lifecycleOwner, stateHandler)

/**
 * Connect to an observable for state updates.
 */
fun <T> Observable<T>.connect(lifecycleOwner: LifecycleOwner, stateHandler: (T) -> Unit = {}): Disposable =
        this.connect(lifecycleOwner.scope(), stateHandler)

/**
 * Connect to a store, will be disconnected using autoDispose.
 */
fun <VS : State> Store<VS>.actions(lifecycleOwner: LifecycleOwner, actionsHandler: (Action) -> Unit = {}): Disposable =
        actions.actions(lifecycleOwner.scope(), actionsHandler)


/**
 * Connect to a store, will be disconnected using autoDispose.
 */
fun <VS : State> Store<VS>.actions(scopeProvider: ScopeProvider, actionsHandler: (Action) -> Unit = {}): Disposable =
        actions.actions(scopeProvider, actionsHandler)

fun <T> Observable<T>.actions(lifecycleOwner: LifecycleOwner, actionsHandler: (T) -> Unit = {}): Disposable =
        actions(lifecycleOwner.scope(), actionsHandler)

/**
 * Connect to an observable of event, will be disconnected using autoDispose. Will emit duplicate events.
 */
fun <T> Observable<T>.actions(scopeProvider: ScopeProvider, actionsHandler: (T) -> Unit = {}): Disposable =
        this.observeOn(AndroidSchedulers.mainThread())
                .autoDispose(scopeProvider)
                .subscribe { actionsHandler(it) }

/**
 * Send actions to the store
 */
fun <VS : State> Store<VS>.events(lifecycleOwner: LifecycleOwner, eventCreator: () -> Observable<Event>): Disposable =
        events(lifecycleOwner.scope(), eventCreator())

fun <VS : State> Store<VS>.events(scopeProvider: ScopeProvider, eventsObservable: Observable<Event>): Disposable =
        eventsObservable.doOnNext { dispatch(it) }.autoDispose(scopeProvider).subscribe()