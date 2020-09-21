package uk.co.oliverdelange.bugs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.picker1.view.*

class MainActivity : AppCompatActivity() {

    var d: MaterialDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        thisWorks.setupIntRange(10..100, 50)
        alsoWorks.picker1_1.setupIntRange(10..100, 50)
    }

    override fun onStart() {
        super.onStart()
        buttonb.setOnClickListener {
            NormalDialog().show(supportFragmentManager, "TAG")
        }
        buttona.setOnClickListener {
            d = MaterialDialog(this).show {
                title(text = "Cut off picker")
                picker1_i(prefill = 50, values = 10..100)
            }
        }
    }
}

fun MaterialDialog.picker1_i(
    prefill: Int = 0,
    values: IntRange = 1..10
): MaterialDialog {
    customView(R.layout.picker1, scrollable = true)
    getCustomView().apply {
        picker1_1.setupIntRange(1..5, 3)
    }
    return this
}

fun NumberPicker.setupIntRange(values: IntRange, prefillIndex: Int) {
    minValue = values.first
    maxValue = values.last
    value = prefillIndex
}

class NormalDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val view = requireActivity().layoutInflater.inflate(R.layout.picker1, null)
            view.picker1_1.setupIntRange(1..100, 50)
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("Test")
            builder.setView(view)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
