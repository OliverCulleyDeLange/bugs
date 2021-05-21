package uk.co.oliverdelange.bugs

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.system.exitProcess

open class BugsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BugsApp)
            modules(listOf(
                module {
                    single { SomeDependency() }
                    single { RustBinding(-1) }
                }
            ))
        }
    }
}

class MainActivity : AppCompatActivity() {

    val dependency: SomeDependency by inject()
    val rust: RustBinding by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        welcome.text = "${dependency.someFun()}.${SomeObject.doThing()}.${RustBinding.doIt1()}.${rust.doIt2()}"
    }
}

class SomeDependency {
    fun someFun(): String {
        Log.i("BUGS", "Some fun happened")
        return "someFun"
    }
}

object SomeObject {
    fun doThing(): String {
        Log.w("BUGS", "Do thing happened")
        return "doThing"
    }
}

class RustBinding constructor(val self: Long) {
    companion object {
        init {
            try {
                System.loadLibrary("someLib")
            } catch (e: UnsatisfiedLinkError) {
                System.err.println("Native code library failed to load when creating ${this.javaClass.name} ")
                e.printStackTrace()
                exitProcess(1)
            }
        }
        @JvmStatic private external fun _doIt1(): String
        @JvmStatic private external fun _doIt2(): String
        fun doIt1(): String {
            Log.w("BUGS", "Do it 1 happened")
            return _doIt1()
        }
    }
    fun doIt2() = RustBinding._doIt2()
}