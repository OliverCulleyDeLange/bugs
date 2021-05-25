package com.rockspin.rxredux.core

import io.reactivex.Observable
import io.reactivex.rxkotlin.toObservable


data class EpicParams<A : Action, S: State>(val actions: Observable<A>, val state: Observable<S>)
typealias Epic<A, S> = (EpicParams<A, S>) -> Observable<Action>

/**
 * Takes the passed in epic, feeds in the action and state from the store and dispatches the results back to the store
 * Epic = Actions in, Actions out.
 */
inline fun <S : State> createEpicMiddleware(crossinline epic: Epic<Action, S>): Middleware<S> = { store ->
    val sideEffects: Observable<Action> = epic(EpicParams(store.actions, store.updates))
    sideEffects.doOnNext { store.dispatch(it) }
            .ignoreElements()
}

fun <S : State> Epic<Action, S>.toMiddleware(): Middleware<S> = createEpicMiddleware(this)

/** Syntactic sugar for labelling epics, otherwise they'de be an anon func */
fun <A : Action, S : State> epic(epic: Epic<A, S>): Epic<A, S> = epic

fun <A : Action, S : State> combineEpics(label: String = "", vararg epic: Epic<A, S>): Epic<A, S> = { epicParams ->
//    Timber.d("Combining ${epic.size} epics: '$label' epics")
    epic.toObservable().flatMap { it(epicParams) }
}
