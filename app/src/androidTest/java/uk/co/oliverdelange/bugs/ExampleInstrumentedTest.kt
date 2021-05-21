package uk.co.oliverdelange.bugs

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @get:Rule
    var activityRule = ActivityTestRule(MainActivity::class.java, false, false)

    val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun useAppContext() {
        assertEquals("uk.co.oliverdelange.bugs", targetContext.packageName)

        activityRule.launchActivity(null)
        onView(withId(R.id.welcome)).check(matches(withText("Hello World!")))
    }
}