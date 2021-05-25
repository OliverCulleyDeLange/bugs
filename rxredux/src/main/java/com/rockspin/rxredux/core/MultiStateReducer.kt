package com.rockspin.rxredux.core


/**
 * A class that manages a state of
 * @param create a function that returns new [State].
 * @param states a map of string to [State]s
 */
data class MultiState<K, VS>(private val create: (K) -> VS, val states: Map<K, VS> = mapOf()) :
        State {

    fun getOrCreate(key: K): VS = states.getOrElse(key, { create(key) })
}

fun <K, VS : State> Reducer<VS>.toMultiStateReducer(keySelector: (result: Action) -> K?) =
        multiStateReducer(keySelector, this)

fun <K, VS : State> multiStateReducer(
        keySelector: (result: Action) -> K?,
        wrappedReducer: Reducer<VS>
): Reducer<MultiState<K, VS>> {

    return { oldState, result ->
        val key = keySelector(result)

        val map = if (key == null) {
            // send the result to every reducer
            oldState.states.plus(
                    oldState.states.entries.map { Pair(it.key, wrappedReducer(it.value, result)) }
            )
        } else {
            // send the result to one reducer
            val reduceToState = wrappedReducer(oldState.getOrCreate(key), result)
            oldState.states.plus(Pair(key, reduceToState))
        }
        oldState.copy(states = map)
    }

}


fun <VS : State> filterReducer(
        keySelector: (result: Action) -> Boolean,
        wrappedReducer: Reducer<VS>
): Reducer<VS> {

    return { oldState, result ->
        val key = keySelector(result)

        if (key) {
            wrappedReducer(oldState, result)
        } else {
            oldState
        }
    }
}


fun <VS : State> Reducer<VS>.filter(keySelector: (result: Action) -> Boolean) =
        filterReducer(keySelector, this)
