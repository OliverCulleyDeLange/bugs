package com.motionmetrics.carv

import com.instabug.library.InstabugState
import com.instabug.library.InstabugStateProvider
import timber.log.Timber

fun state() = InstabugStateProvider.getInstance().state
fun ready() = state() == InstabugState.ENABLED

class InstabugWaiter(val prefs: DevPreferences) {
    var initStarted: Long = System.currentTimeMillis()

    fun maybeWaitForInstabug() {
        if (prefs.shouldWaitForInstabug) waitForInstabug()
    }

    fun waitForInstabug() {
        Timber.w("Waiting for instabug to become ready before continuing. Timeout: $INIT_TIMEOUT_MS")
        initStarted = System.currentTimeMillis()
        while (!ready() && notTimedOut(System.currentTimeMillis())) {
            Timber.w("LOOP @ ${System.currentTimeMillis()}: Instabug ready: ${ready()} state: ${state()}")
            Thread.sleep(25)
        }
        Timber.w("WAITED FOR INSTABUG. ready: ${ready()} state: ${state()}")
    }

    private fun notTimedOut(currentMs: Long): Boolean {
        return currentMs <= initStarted + INIT_TIMEOUT_MS
    }

    companion object {
        private const val INIT_TIMEOUT_MS = 200
    }
}