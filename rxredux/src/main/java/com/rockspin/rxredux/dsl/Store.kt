package com.rockspin.rxredux.dsl

import com.rockspin.rxredux.core.*


fun <S : State> S.toStore(builder: StoreBuilder<S>.() -> Unit): Store<S> {
    val storeBuilder = StoreBuilder<S>()
    storeBuilder.state { this }
    builder(storeBuilder)
    return storeBuilder.build()
}


@DslMarker
annotation class StoreBuilderMarker

@StoreBuilderMarker
class StoreBuilder<S : State> {
    private var state: S? = null
    private var reducerBuilder: ReducerBuilder<S> = ReducerBuilder()
    private var middleware: Middleware<S>? = null

    fun state(state: () -> S) {
        this.state = state()
    }

    fun middleware(middleware: Middleware<S>) {
        this.middleware = middleware
    }

    /**
     * append a reducer to this store
     * @param function builder function this is bound to the current state.
     */
    fun reduce(function: S.(action: Action) -> S) = reducerBuilder.createReducer(function)

    fun reducers(addBuilders: ReducerBuilder<S>.() -> Unit = {}) = reducerBuilder.addBuilders()

    fun build(): Store<S> = createStore(
            reducerBuilder.build(),
            middleware,
            state!!
    )

}