package uk.co.oliverdelange.bugs

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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

open class MainActivity : AppCompatActivity() {

    val dependency: SomeDependency by inject()
    val rust: RustBinding by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        welcome.text =
            "${this.localClassName}.${dependency.someFun()}.${SomeObject.doThing()}.${RustBinding.doIt1()}.${rust.doIt2()}"
        if (this is SecondaryActivity) {
            next.isVisible = false
        } else next.setOnClickListener {
            startActivity(Intent(this, SecondaryActivity::class.java))
        }
    }
}

class SecondaryActivity : MainActivity()

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
        @JvmStatic
        private external fun _doIt1(): String

        @JvmStatic
        private external fun _doIt2(): String
        fun doIt1(): String {
            Log.w("BUGS", "Do it 1 happened")
            return try {
                _doIt1()
            } catch (E: Throwable) {
                Log.w("BUGS", "Do it 1 failed, falling back")
                "doIt1 failed"
            }
        }
    }

    fun doIt2(): String = try {
        RustBinding._doIt2()
    } catch (E: Throwable) {
        Log.w("BUGS", "Do it 2 failed, falling back")
        "doIt2 failed"
    }
}