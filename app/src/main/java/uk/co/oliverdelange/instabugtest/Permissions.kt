package uk.co.oliverdelange.instabugtest

import android.Manifest
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable

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
            }
            permission.shouldShowRequestPermissionRationale -> {
                Log.i(":::", "SHOW RATIONALE $permission")
            }
            else -> {
                Log.i(":::", "DENIED $permission")
            }
        }
    }