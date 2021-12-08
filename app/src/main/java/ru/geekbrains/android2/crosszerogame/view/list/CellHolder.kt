package ru.geekbrains.android2.crosszerogame.view.list

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R

class CellHolder(
    private val view: View,
    private val onItemClick: ((Int, Int) -> Unit)
) : RecyclerView.ViewHolder(view) {
    companion object {
        var cellSize = 0
    }

    init {
        view.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ ->
            if (cellSize == 0)
                cellSize = v.width
            if (v.height != cellSize) {
                v.layoutParams = v.layoutParams.apply {
                    this.height = cellSize
                }
            }
        }
    }

    fun setItem(cell: Cell) {
        val iv = view.findViewById(R.id.iv_cell) as ImageView
        when (cell.value) {
            CellValue.EMPTY -> iv.setImageResource(0)
            CellValue.CROSS -> iv.setImageResource(R.drawable.ic_cross)
            CellValue.ZERO -> iv.setImageResource(R.drawable.ic_zero)
        }
        view.setOnClickListener {
            if (cell.value == CellValue.EMPTY)
                onItemClick(cell.coord.x, cell.coord.y)
        }
    }
}