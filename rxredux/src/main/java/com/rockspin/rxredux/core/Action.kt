package com.rockspin.rxredux.core

import android.util.Log

/**
 * An action modifies state
 */
interface Action {
    fun shouldLog() = true
    fun log() = Log.VERBOSE to toString()
}

/**
 * An action that is dispatched after a user action, or request to do something by the system
 */
interface Event : Action {
    override fun log() = Log.INFO to super.log().second
}

/**
 * An action that is created as a side effect or result of an event
 */
interface Result : Action {
    override fun log() = Log.DEBUG to super.log().second
}

/**
 * Sub classes should be a data class that represents state with in an application.
 */
interface State