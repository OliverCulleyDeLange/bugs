package uk.co.oliverdelange.bugs

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import timber.log.Timber


class TimberConsoleExtension : BeforeAllCallback, AfterAllCallback {

    private val consoleTree = object : Timber.DebugTree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            println(message)
        }
    }

    override fun beforeAll(context: ExtensionContext) = Timber.plant(consoleTree)
    override fun afterAll(context: ExtensionContext) = Timber.uproot(consoleTree)
}

@ExtendWith(TimberConsoleExtension::class)
internal class MainActivityTest {
    @org.junit.jupiter.api.Nested
    inner class Nested {

        @Test
        fun beforeAllCalledTwice() {
            Timber.e("Log")
        }
    }
}