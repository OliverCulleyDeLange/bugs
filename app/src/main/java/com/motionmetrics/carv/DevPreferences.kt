package com.motionmetrics.carv

import android.content.Context

class DevPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val SHARED_PREFS_NAME = "DEV_PREF"
        const val PREF_DEV_SHOULD_WAIT_FOR_INSTABUG = "PREF_DEV_SHOULD_WAIT_FOR_INSTABUG"
    }

    var shouldWaitForInstabug: Boolean
        get() = prefs.getBoolean(PREF_DEV_SHOULD_WAIT_FOR_INSTABUG, false)
        set(value) = prefs.edit().putBoolean(PREF_DEV_SHOULD_WAIT_FOR_INSTABUG, value).apply()

}