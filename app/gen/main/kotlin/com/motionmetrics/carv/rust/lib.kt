package com.motionmetrics.carv.rust
import kotlin.system.exitProcess

class App constructor (val self: Long) {
    companion object {
        init {
            try {
                System.loadLibrary("android")
            } catch (e: UnsatisfiedLinkError) {
                System.err.println("Native code library failed to load when creating ${this.javaClass.name} ")
                e.printStackTrace()
                exitProcess(1)
            }
        }

        @JvmStatic private external fun _force_panic()
        @JvmStatic private external fun destroy(self: Long)

        fun forcePanic() {
            App._force_panic()
        }

    }

    protected fun finalize() {
        App.destroy(self)
    }
}