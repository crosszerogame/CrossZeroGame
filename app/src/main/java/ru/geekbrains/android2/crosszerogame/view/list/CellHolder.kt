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
        var cellSize = 100
    }

    init {
        view.layoutParams = view.layoutParams.apply {
            this.width = cellSize
            this.height = cellSize
        }
    }

    fun setItem(x: Int, y: Int, cell: CellValue) {
        val iv = view.findViewById(R.id.iv_cell) as ImageView
        when (cell) {
            CellValue.EMPTY -> iv.setImageResource(0)
            CellValue.CROSS -> iv.setImageResource(R.drawable.ic_cross)
            CellValue.ZERO -> iv.setImageResource(R.drawable.ic_zero)
        }
        view.setOnClickListener {
            if (cell == CellValue.EMPTY)
                onItemClick(x, y)
        }
    }
}