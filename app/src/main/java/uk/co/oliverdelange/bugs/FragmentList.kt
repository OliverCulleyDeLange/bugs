package uk.co.oliverdelange.bugs

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.GroupieViewHolder
import com.xwray.groupie.kotlinandroidextensions.Item
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.item.view.*

class FragmentList : Fragment() {
    val manager by lazy { LinearLayoutManager(requireContext()) }
    val adapter = GroupAdapter<GroupieViewHolder>()
    val items = (1..100).map { i ->
        MyItem(i) {
            swiperefresh.isRefreshing = false
            Toast.makeText(context, "Clicked $i!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_list, container, false)

    override fun onStart() {
        super.onStart()
        swiperefresh.setOnRefreshListener {
            Log.v("TEST", "Pull to refresh")
        }

        recycler.layoutManager = manager
        recycler.adapter = adapter

        adapter.update(items)

        recycler.scrollToPosition(50) // Works, but not with ViewPager2
    }
}

class MyItem(val i: Int, val onClick: () -> Unit) : Item() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Log.w("TEST", "Binding $i $position")
        viewHolder.itemView.textView.text = i.toString()
        viewHolder.itemView.setOnClickListener {
            Log.w("TEST", "Clicked $i $position")
            onClick()
        }
    }

    override fun unbind(viewHolder: GroupieViewHolder) {
        super.unbind(viewHolder)
        Log.w("TEST", "Unbinding $i")
    }

    override fun getLayout() = R.layout.item

}