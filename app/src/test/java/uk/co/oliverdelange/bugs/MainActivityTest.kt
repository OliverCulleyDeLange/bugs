package uk.co.oliverdelange.bugs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import timber.log.Timber


class Ext : BeforeAllCallback, AfterAllCallback {
    override fun beforeAll(context: ExtensionContext) = println("beforeAll")
    override fun afterAll(context: ExtensionContext) = println("afterAll")
}

@ExtendWith(Ext::class)
internal class MainActivityTest {
    @org.junit.jupiter.api.Nested
    inner class Nested {

        @Test
        fun beforeAllCalledTwice() {
            /** Out put is:
            beforeAll
            beforeAll


            afterAll
            afterAll
             */
        }
    }
}