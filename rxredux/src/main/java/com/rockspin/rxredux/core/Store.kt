package com.rockspin.rxredux.core

import com.jakewharton.rx.replayingShare
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo

typealias Middleware<S> = (store: Store<S>) -> Completable


/**
 * A class that can be extended to create a DI friendly store
 */
abstract class DiStore<VS : State>(
        reducer: Reducer<VS>,
        middleware: Middleware<VS>,
        initialState: VS
) : Store<VS> by createStore(reducer, middleware, initialState)


interface Store<VS : State> {

    /**
     * Returns the current state
     */
    fun currentViewState(): VS

    /**
     * Stream of updates to the store, emits state immediately on subscription.
     */
    val updates: Observable<VS>

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
    fun dispatch(action: Action)

    /**
     * A stream of all actions dispatched via the dispatcher
     */
    val actions: Observable<Action>

    /**
     * Destroys this store. Will not emit or update state after this has been called.
     */
    fun destroy()
}

/**
 * Creates a Redux store that holds the state.
 * The only way to change the data in the store is to call `dispatch()` on it or the [Dispatcher].
 *
 * There should only be a single store in your app. To specify how different
 * parts of the state tree respond to actions, you may combine several reducers
 * into a single reducer function by using `combineReducers`.
 *
 * @param reducer A function that returns the next state tree, given
 * the current state tree and the action to handle.
 *
 * @param middleware The store enhancer. You may optionally specify it
 * to enhance the store with third-party capabilities such as middleware,
 * time travel, persistence, etc. The only store enhancer that ships with Redux
 * is `applyMiddleware()`.
 *
 *  @param initialState The initial state.
 *
 * @returns A Redux store that lets you read the state, dispatch actions
 * and subscribe to changes.
 */
fun <VS : State> createStore(
        reducer: Reducer<VS>,
        middleware: Middleware<VS>?,
        initialState: VS
): Store<VS> = BasicStore(initialState, reducer, middleware)

private class BasicStore<VS : State>(
        initialState: VS,
        private val reducer: Reducer<VS>,
        middleware: Middleware<VS>?
) : Store<VS> {

    private val compositeDisposeable = CompositeDisposable()

    override val updates = Dispatcher.actions
            .scan(initialState) { oldState, result -> reducer(oldState, result) }
            //FIXME Doing equality check on massive objects like Day will cause mega slow downs
            .distinctUntilChanged()
            .replayingShare()

    override val actions: Observable<Action> = Dispatcher.actions

    init {
        // connect to the updates so that the store is always providing updates.
        updates.subscribe().addTo(compositeDisposeable)
        val createdMiddleware = middleware?.invoke(this)
        createdMiddleware?.subscribe()?.addTo(compositeDisposeable)
    }

    override fun currentViewState(): VS {
        check(!compositeDisposeable.isDisposed) { "Can't read current state the store is destroyed" }
        // since updates are created with replaying share this will always emit a value
        // in a thread safe way.
        return updates.blockingFirst()
    }

    override fun dispatch(action: Action) {
        check(!compositeDisposeable.isDisposed) { "Can't dispatch the store is destroyed" }
        Dispatcher.dispatch(action)
    }

    override fun destroy() {
        compositeDisposeable.dispose()
    }

}