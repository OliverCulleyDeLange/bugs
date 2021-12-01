package uk.co.oliverdelange.instabugtest

import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.instabug.crash.CrashReporting
import com.instabug.library.Feature
import com.instabug.library.Instabug
import com.instabug.library.invocation.InstabugInvocationEvent

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Instabug.isBuilt()) {
            Log.v("DEBUG", "Initialising instabug")
            Instabug.Builder(this, "sopme key")
                .setInvocationEvents(InstabugInvocationEvent.NONE)
                .build()
        }
        CrashReporting.setState(Feature.State.ENABLED)
        CrashReporting.setNDKCrashesState(Feature.State.ENABLED)

        Instabug.setDebugEnabled(true)
    }
}

class MainActivity : FragmentActivity() {

    @SuppressLint("AutoDispose")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener {
            Log.w("TAG", "Showing instabug")
            Instabug.show()
        }
    }
}