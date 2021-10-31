package uk.co.oliverdelange.instabugtest

import android.app.Application
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import com.instabug.library.Instabug
import com.instabug.library.invocation.InstabugInvocationEvent
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import uk.co.oliverdelange.instabugtest.ui.theme.InstabugTestTheme
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicBoolean

public class MyApp : Application(){
    override fun onCreate() {
        super.onCreate()
        Instabug.Builder(this, "some key")
            .setInvocationEvents(
                InstabugInvocationEvent.TWO_FINGER_SWIPE_LEFT,
                InstabugInvocationEvent.SCREENSHOT
            )
            .build()
        Instabug.setDebugEnabled(true)
    }
}

class SyncWorker : Completable(){
    override fun subscribeActual(observer: CompletableObserver?) {
        val atomicBoolean = AtomicBoolean(true)
        observer?.onSubscribe(Disposables.fromAction {
            atomicBoolean.set(false)
        })
        do {
            try {
                Thread.sleep(1000)
            } catch(t: Throwable){
                Log.e("INSTABUG-BUG", "ERR sleeping", t)
            }
        } while (atomicBoolean.get())
    }
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstabugTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting("Android")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SyncWorker().toObservable<Unit>()
            .subscribeOn(Schedulers.single()) // instabug can't be shown
//            .subscribeOn(Schedulers.io()) // Instabug can be shown
            .connect(scope(untilEvent = Lifecycle.Event.ON_PAUSE))
    }
}

fun <T> Observable<T>.connect(scope: ScopeProvider, stateHandler: (T) -> Unit = {}): Disposable =
    this
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .autoDispose(scope)
        .subscribe(stateHandler)


@Composable
fun Greeting(name: String) {
    Button(onClick = { Instabug.show() }) {
        Text(text = "Show")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    InstabugTestTheme {
        Greeting("Android")
    }
}