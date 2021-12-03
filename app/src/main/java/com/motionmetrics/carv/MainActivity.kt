package com.motionmetrics.carv

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.instabug.crash.CrashReporting
import com.instabug.library.Feature
import com.instabug.library.Instabug
import com.instabug.library.InstabugState
import com.instabug.library.InstabugStateProvider
import com.instabug.library.invocation.InstabugInvocationEvent
import com.motionmetrics.carv.rust.App
import io.reactivex.Observable
import timber.log.Timber
import uk.co.oliverdelange.instabugtest.R
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.random.Random.Default.nextBoolean

class MyApp : Application() {
    @SuppressLint("CheckResult")
    override fun onCreate() {
        super.onCreate()
        Log.v(":::", "Initialising instabug")
        Instabug.Builder(this, "")//beta key
            .setInvocationEvents(InstabugInvocationEvent.NONE)
            .setDebugEnabled(true)
            .build()
        val state = InstabugStateProvider.getInstance().state
        Log.v(":::", "Initialised instabug $state")
        CrashReporting.setState(Feature.State.ENABLED)
        CrashReporting.setNDKCrashesState(Feature.State.ENABLED)
        Timber.plant(CustomDebugTree())

        val prefs = DevPreferences(this)
        InstabugWaiter(prefs).maybeWaitForInstabug()

        prefs.shouldWaitForInstabug = true
        if (nextBoolean()) {
            Timber.w("FORCING PANIC ON CREATE")
            App.forcePanic()
        }
        prefs.shouldWaitForInstabug = false
    }
}

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.crash_native).setOnClickListener {
            Timber.w("FORCING PANIC ON ACTION")
            App.forcePanic()
        }
        findViewById<Button>(R.id.crash_android).setOnClickListener {
            Timber.w("FORCING CRASH ON ACTION")
            throw RuntimeException("Crashy crash crash")
        }
    }
}

class CustomDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        val currentThread = Thread.currentThread()
        val tID = currentThread.id
        val tName = currentThread.name
        val threadInfo = "(${tName.padStart(6).take(6)}:$tID)"
        return ":::IB-T:::$threadInfo" + super.createStackElementTag(element)
    }
}