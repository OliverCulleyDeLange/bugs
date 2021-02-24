package uk.co.oliverdelange.bugs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import io.flutter.embedding.android.FlutterFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG_FLUTTER_FRAGMENT: String = "flutter_view"
    private val TAG_FRAGMENT: String = "fragment"

    private var flutterFragment: FlutterFragment? = null
    private var fragment: AFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button1.setOnClickListener {
            flutterView.isVisible = true
            fragmentView.isVisible = true
        }
        button2.setOnClickListener {
            flutterView.isVisible = false
            fragmentView.isVisible = false
        }

        flutterFragment = supportFragmentManager.findFragmentByTag(TAG_FLUTTER_FRAGMENT) as FlutterFragment?
        if (flutterFragment == null) {
            val f = FlutterFragment.createDefault()
            flutterFragment = f
            supportFragmentManager.beginTransaction()
                .add(R.id.flutterView, f, TAG_FLUTTER_FRAGMENT)
                .commit()
        }

        fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) as AFragment?
        if (fragment == null) {
            val f = AFragment()
            fragment = f
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentView, f, TAG_FRAGMENT)
                .commit()
        }
    }
}

class AFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment, container, false)
    }
}