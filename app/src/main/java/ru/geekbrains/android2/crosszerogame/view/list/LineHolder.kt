package ru.geekbrains.android2.crosszerogame.view.list

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R

class LineHolder(
    private val view: View,
    private val linear: Linear
) : RecyclerView.ViewHolder(view) {
    fun setItem(adapter: LineAdapter) {
        val rv = view.findViewById(R.id.rv_line) as RecyclerView
        val layoutManager = LinearLayoutManager(
            rv.context,
            if (linear == Linear.HORIZONTAL)
                LinearLayoutManager.HORIZONTAL
            else
                LinearLayoutManager.VERTICAL,
            false
        )
        rv.layoutManager = layoutManager
        rv.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}