package ru.geekbrains.android2.crosszerogame.view.list

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R

class CellHolder(
    private val view: View,
    private val onItemClick: ((Int, Int) -> Unit)
) : RecyclerView.ViewHolder(view) {
    companion object {
        var cellSize = 100
    }

    init {
        view.layoutParams = view.layoutParams.apply {
            this.width = cellSize
            this.height = cellSize
        }
    }

    fun setItem(x: Int, y: Int, cell: Cell) {
        val iv = view.findViewById(R.id.iv_cell) as ImageView
        when (cell.value) {
            Cell.Value.EMPTY -> iv.setImageResource(0)
            Cell.Value.CROSS -> iv.setImageResource(R.drawable.cross)
            Cell.Value.ZERO -> iv.setImageResource(R.drawable.zero)
        }
        when (cell.position) {
            Cell.Position.CENTER -> view.setBackgroundResource(R.drawable.cell)
            Cell.Position.TOP -> view.setBackgroundResource(R.drawable.cell_top)
            Cell.Position.BOTTOM -> view.setBackgroundResource(R.drawable.cell_bottom)
            Cell.Position.TOP_LEFT -> view.setBackgroundResource(R.drawable.cell_top_left)
            Cell.Position.TOP_RIGHT -> view.setBackgroundResource(R.drawable.cell_top_right)
            Cell.Position.BOTTOM_LEFT -> view.setBackgroundResource(R.drawable.cell_bottom_left)
            Cell.Position.BOTTOM_RIGHT -> view.setBackgroundResource(R.drawable.cell_bottom_right)
            Cell.Position.LEFT -> view.setBackgroundResource(R.drawable.cell_left)
            Cell.Position.RIGHT -> view.setBackgroundResource(R.drawable.cell_right)
        }
        view.setOnClickListener {
            if (cell.value == Cell.Value.EMPTY)
                onItemClick(x, y)
        }
    }
}