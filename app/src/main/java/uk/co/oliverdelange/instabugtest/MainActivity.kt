package uk.co.oliverdelange.instabugtest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.google.android.material.snackbar.Snackbar
import com.instabug.library.Instabug
import com.instabug.library.invocation.InstabugInvocationEvent
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
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
import java.util.concurrent.atomic.AtomicBoolean

public class MyApp : Application() {
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

class SyncWorker : Completable() {
    override fun subscribeActual(observer: CompletableObserver?) {
        val atomicBoolean = AtomicBoolean(true)
        observer?.onSubscribe(Disposables.fromAction {
            atomicBoolean.set(false)
        })
        do {
            try {
                Thread.sleep(1000)
            } catch (t: Throwable) {
                Log.e("INSTABUG-BUG", "ERR sleeping", t)
            }
        } while (atomicBoolean.get())
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
        permissionRequest(this).subscribe()
    }

    override fun onResume() {
        super.onResume()
        SyncWorker().toObservable<Unit>()
            .subscribeOn(Schedulers.single()) // Instabug can't be shown on v9.0.5
//            .subscribeOn(Schedulers.io()) // Instabug can be shown on v9.0.5
            .connect(scope(untilEvent = Lifecycle.Event.ON_PAUSE))
    }

    fun permissionRequest(activity: FragmentActivity): Observable<Permission> {
        val rxPermissions = RxPermissions(activity).apply { setLogging(true) }
        return doPermissionRequest(rxPermissions)
    }

    private fun doPermissionRequest(rxPermissions: RxPermissions) = rxPermissions
        .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        .doOnNext { permission ->
            when {
                permission.granted -> {
                    Log.i(":::", "GRANTED $permission")
                    Snackbar.make(findViewById(R.id.root), "Granted $permission", Snackbar.LENGTH_SHORT).show()
                }
                permission.shouldShowRequestPermissionRationale -> {
                    Log.i(":::", "SHOW RATIONALE $permission")
                    Snackbar.make(findViewById(R.id.root), "Rationale required for $permission", Snackbar.LENGTH_SHORT).show()
                }
                else -> {
                    Log.i(":::", "DENIED $permission")
                    Snackbar.make(findViewById(R.id.root), "DENIED $permission", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
}

fun <T> Observable<T>.connect(scope: ScopeProvider, stateHandler: (T) -> Unit = {}): Disposable =
    this
        .distinctUntilChanged()
        .observeOn(AndroidSchedulers.mainThread())
        .autoDispose(scope)
        .subscribe(stateHandler)