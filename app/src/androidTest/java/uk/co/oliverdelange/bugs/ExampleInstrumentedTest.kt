package uk.co.oliverdelange.bugs

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnitRunner
import io.mockk.*

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.reflect.KClass

class EspressoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@EspressoApp)
            modules(listOf(
                module {
                    single { mockk<SomeDependency>() }
                    single { mockk<RustBinding>() }
                }
            ))
        }
    }
}

/** Referenced in app/build.gradle in android -> defaultConfig -> testInstrumentationRunner */
class TestAppJUnitRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
//         https://github.com/tmurakami/dexopener
//         https://github.com/tmurakami/dexopener/blob/master/examples/mockk/src/androidTest/java/com/example/dexopener/mockk/MyAndroidJUnitRunner.kt
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Log.w("TestAppJUnitRunner", "Opening Dex as we're running on API ${Build.VERSION.SDK_INT}")
//            DexOpener.install(this)
        } else {
            Log.i("TestAppJUnitRunner", "Running API ${Build.VERSION.SDK_INT}")
        }
        // https://github.com/tmurakami/dexopener/issues/23
        val testApp = "uk.co.oliverdelange.bugs.EspressoApp"
        Log.i("TestAppJUnitRunner", "Creating new application for Espresso Testing: $testApp")
        return super.newApplication(cl, testApp, context)
    }
}

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun useAppContext() {
        assertEquals("uk.co.oliverdelange.bugs", targetContext.packageName)

        val app = targetContext.applicationContext as Application
        val koin = app.getKoin()

        val someDep = koin.get<SomeDependency>()

        every { someDep.someFun() } returns "1"

        mockkObject(SomeObject)
        every { SomeObject.doThing() } returns "2"

        mockkObject(RustBinding)
        every { RustBinding.doIt1() } returns "3"

        val rust = koin.get<RustBinding>()
        every { rust.doIt2() } returns "4"

        activityRule.launchActivity(null)
        onView(withId(R.id.welcome)).check(matches(withText("1.2.3.4")))
    }
}