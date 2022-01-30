package ru.geekbrains.android2.crosszerogame.view.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R

class LineAdapter(
    private val index: Int,
    length: Int,
    private val linear: Linear,
    private val onItemClick: ((Int, Int) -> Unit)
) : RecyclerView.Adapter<CellHolder>() {
    private val cells = mutableListOf<Cell>()
    private val max = length - 1

    init {
        println(linear)
        for (i in 0 until length) {
            val cell = Cell(Cell.Value.EMPTY, getPositionFor(i))
            cells.add(cell)
        }
    }

    private fun getPositionFor(i: Int): Cell.Position {
        return when (i) {
            0 -> when (index) {
                0 -> // x=0 y=0
                    Cell.Position.TOP_LEFT
                max ->
                    if (linear == Linear.HORIZONTAL) // x=0 y=max
                        Cell.Position.TOP_RIGHT
                    else  // x=max y=0
                        Cell.Position.BOTTOM_LEFT
                else ->
                    if (linear == Linear.HORIZONTAL)  // x=0 y=n
                        Cell.Position.TOP
                    else  // x=n y=0
                        Cell.Position.LEFT
            }
            max -> when (index) {
                max ->  // x=max y=max
                    Cell.Position.BOTTOM_RIGHT
                0 ->
                    if (linear == Linear.HORIZONTAL)  // x=max y=0
                        Cell.Position.BOTTOM_LEFT
                    else  // x=0 y=max
                        Cell.Position.TOP_RIGHT
                else ->
                    if (linear == Linear.HORIZONTAL) // x=max y=0
                        Cell.Position.BOTTOM
                    else  // x=n y=max
                        Cell.Position.RIGHT
            }
            else -> when (index) {
                0 ->
                    if (linear == Linear.HORIZONTAL)  // x=n y=0
                        Cell.Position.LEFT
                    else  // x=0 y=n
                        Cell.Position.TOP
                max ->
                    if (linear == Linear.HORIZONTAL)  // x=n y=max
                        Cell.Position.RIGHT
                    else  // x=max y=n
                        Cell.Position.BOTTOM
                else -> // x=n y=n
                    Cell.Position.CENTER
            }
        }
    }

    fun setCrossOn(i: Int) {
        cells[i] = Cell(Cell.Value.CROSS, cells[i].position)
        notifyItemChanged(i)
    }

    fun setZeroOn(i: Int) {
        cells[i] = Cell(Cell.Value.ZERO, cells[i].position)
        notifyItemChanged(i)
    }

    fun setCell(i: Int, value: Cell.Value) {
        cells[i] = Cell(value, cells[i].position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CellHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_cell,
                parent, false
            ),
            onItemClick
        )

    override fun onBindViewHolder(holder: CellHolder, position: Int) =
        if (linear == Linear.HORIZONTAL)
            holder.setItem(index, position, cells[position])
        else
            holder.setItem(position, index, cells[position])

    override fun getItemCount() = cells.size
}