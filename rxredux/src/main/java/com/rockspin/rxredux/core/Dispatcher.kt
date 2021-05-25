package com.rockspin.rxredux.core

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import timber.log.Timber

object Dispatcher {
    private val resultsRelay = PublishRelay.create<Action>()


    /**
     * Dispatches an action. It is the only way to trigger a state change.
     *
     * The [Reducer] function, used to create the store, will be called with the
     * current state and the given [action]. Its return value will be
     * considered the **next** state.
     *
     * The base implementation only supports plain object actions. If you want
     * to dispatch a Promise, an Observable, a thunk, or something else, you
     * need to wrap your store creating function into the corresponding
     * middleware. For example, see [Epic]
     *
     * @param action A plain object representing “what changed”. It is a good
     *   idea to keep actions serializable so you can record and replay user
     *   sessions
     *
     * Note that, if you use a custom middleware, it may wrap [dispatch]`.
     */
    fun dispatch(action: Action) {
        resultsRelay.accept(action)
    }

    /**
     * A stream of all actions dispatched via the dispatcher
     */
    val actions: Observable<Action> = resultsRelay.hide()
}


