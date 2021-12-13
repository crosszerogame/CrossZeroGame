package ru.geekbrains.android2.crosszerogame.view.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R

class FieldAdapter(
    fieldSize: Int,
    cellSize: Int,
    val linear: Linear,
    onItemClick: ((Int, Int) -> Unit)
) : RecyclerView.Adapter<LineHolder>() {
    private val lines = mutableListOf<LineAdapter>()

    init {
        CellHolder.cellSize = cellSize
        for (i in 0 until fieldSize) {
            lines.add(LineAdapter(fieldSize, i, linear, onItemClick))
        }
    }

    fun setCrossOn(x: Int, y: Int) {
        if (linear == Linear.HORIZONTAL)
            lines[x].setCrossOn(y)
        else
            lines[y].setCrossOn(x)
    }

    fun setZeroOn(x: Int, y: Int) {
        if (linear == Linear.HORIZONTAL)
            lines[x].setZeroOn(y)
        else
            lines[y].setZeroOn(x)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LineHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_line,
                parent, false
            ),
            if (linear == Linear.HORIZONTAL)
                Linear.VERTICAL
            else
                Linear.HORIZONTAL
        )

    override fun onBindViewHolder(holder: LineHolder, position: Int) =
        holder.setItem(lines[position])

    override fun getItemCount() = lines.size
}