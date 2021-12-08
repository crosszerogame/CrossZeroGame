package ru.geekbrains.android2.crosszerogame.view.list

import android.graphics.Point
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R

class GameAdapter(
    private val fieldSize: Int,
    private val onItemClick: ((Int, Int) -> Unit)
) : RecyclerView.Adapter<CellHolder>() {
    private val cells = mutableListOf<Cell>()

    init {
        CellHolder.cellSize = 0
        for (y in 0 until fieldSize) {
            for (x in 0 until fieldSize)
                cells.add(Cell(Point(x, y)))
        }
    }

    fun setCrossOn(x: Int, y: Int) {
        val i = y * fieldSize + x
        cells[i].value = CellValue.CROSS
        notifyItemChanged(i)
    }

    fun setZeroOn(x: Int, y: Int) {
        val i = y * fieldSize + x
        cells[i].value = CellValue.ZERO
        notifyItemChanged(i)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CellHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_field,
                parent, false
            ),
            onItemClick
        )

    override fun onBindViewHolder(holder: CellHolder, position: Int) =
        holder.setItem(cells[position])

    override fun getItemCount() = cells.size
}