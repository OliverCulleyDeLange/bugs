package com.rockspin.rxredux.dsl

import com.rockspin.rxredux.core.Action
import com.rockspin.rxredux.core.Reducer
import com.rockspin.rxredux.core.State
import com.rockspin.rxredux.core.combineReducers


class ReducerBuilder<S : State> {

    private var reducers: List<Reducer<S>> = emptyList()

    fun createReducer(function: S.(action: Action) -> S) {
        this.reducers =
                reducers.plus { oldState, action ->
                    oldState.function(action)
                }
    }

    fun build(): Reducer<S> = combineReducers(reducers)
}


