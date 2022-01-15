package ru.geekbrains.android2.crosszerogame.view.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R


class WelcomeSlidesViewPagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val layouts = intArrayOf(
        R.layout.welcoming_slides_fragment_slide_one,
        R.layout.welcoming_slides_fragment_slide_two,
        R.layout.welcoming_slides_fragment_slide_three,
        R.layout.welcoming_slides_fragment_slide_four
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View = LayoutInflater.from(parent.getContext())
            .inflate(viewType, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        println("Implemented")
    }

    override fun getItemViewType(position: Int): Int {
        return layouts[position]
    }

    override fun getItemCount(): Int {
        return layouts.size
    }

    inner class SliderViewHolder(view: View?) : RecyclerView.ViewHolder(view!!)

}