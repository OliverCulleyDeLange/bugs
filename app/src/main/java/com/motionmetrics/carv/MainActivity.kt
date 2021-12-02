package com.motionmetrics.carv

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.instabug.crash.CrashReporting
import com.instabug.library.Feature
import com.instabug.library.Instabug
import com.instabug.library.invocation.InstabugInvocationEvent
import com.motionmetrics.carv.rust.App
import timber.log.Timber
import uk.co.oliverdelange.instabugtest.R
import kotlin.random.Random.Default.nextBoolean

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(CustomDebugTree())
        Instabug.setDebugEnabled(true)
        Timber.v( "Initialising instabug")
        Instabug.Builder(this, "")//beta key
            .setInvocationEvents(InstabugInvocationEvent.NONE)
            .build()
        Timber.v( "Initialised instabug")

        Timber.v( "Setting feature state")
        Instabug.setDebugEnabled(true)
        CrashReporting.setState(Feature.State.ENABLED)
        CrashReporting.setNDKCrashesState(Feature.State.ENABLED)
        Timber.v( "Set feature state")


        if (nextBoolean()){
            Timber.w( "FORCING PANIC ON CREATE")
            App.forcePanic()
        }
    }
}

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener {
            Timber.w("FORCING PANIC ON ACTION")
            App.forcePanic()
        }
    }
}

class CustomDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String {
        val currentThread = Thread.currentThread()
        val tID = currentThread.id
        val tName = currentThread.name
        val threadInfo = "(${tName.padStart(6).take(6)}:$tID)"
        return ":::TEST:::$threadInfo" + super.createStackElementTag(element)
    }
}