package com.rockspin.rxredux.android

import com.rockspin.rxredux.core.State
import com.rockspin.rxredux.core.Store

abstract class FluxViewModel<VS : State>(val store: Store<VS>) : AutoDisposeViewModel(), Store<VS> by store {

    init {
        store.updates.connect(this)
    }
}
