package uk.co.oliverdelange.bugs

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.polidea.rxandroidble2.RxBleClient
import com.rockspin.rxredux.android.connect
import com.rockspin.rxredux.core.*
import com.rockspin.rxredux.dsl.buildEpic
import com.uber.autodispose.android.lifecycle.scope
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.rxkotlin.ofType
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class AppState(val i: Int) : State

object ButtonClicked : Event
object NavToNextScreen : Event

class AppStore(
    rustBinding: RustBinding,
    someDependency: SomeDependency,
    context: Context
) : DiStore<AppState>(
    createReducer { action ->
        when (action) {
            ButtonClicked -> copy(i = i + 1)
            else -> copy(i = 0)
        }
    },
    buildEpic<Action, AppState>("main") {
        withEpic { (actions, state) ->
            actions.ofType<ButtonClicked>().map {
                NavToNextScreen
            }
        }
        withEpic { (actions) ->
            actions.ofType<ButtonClicked>()
                    //FIXME window operator makes UItests crash with no stack trace
                .window(1, TimeUnit.MINUTES)
                .flatMapSingle { it.count() }
                .doOnNext { Log.w("TESTEST", "COUNT: $it")}
                .ignoreElements().toObservable()
        }
    }.toMiddleware(),
    AppState(i = 1)
)

open class BugsApp : Application() {

    val rust: RustBinding by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@BugsApp)
            modules(listOf(
                module {
                    single { SomeDependency() }
                    single { RustBinding(-1) }
                    single(createdAtStart = true) { AppStore(get(), get(), androidContext()) }
                }
            ))
        }

        Log.i("BUGS", "App Created: ${rust.doIt2()}")
    }
}

open class MainActivity : AppCompatActivity() {

    val dependency: SomeDependency by inject()
    val rust: RustBinding by inject()
    val store: AppStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        welcome.text =
            "${this.localClassName}.${dependency.someFun()}.${SomeObject.doThing()}.${RustBinding.doIt1()}.${rust.doIt2()}"
        if (this is SecondaryActivity) {
            next.isVisible = false
        } else {
            store.actions.connect(scope()) {
                when (it) {
                    NavToNextScreen -> startActivity(Intent(this, SecondaryActivity::class.java))
                }
            }
            next.setOnClickListener {
                store.dispatch(ButtonClicked)
            }
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
