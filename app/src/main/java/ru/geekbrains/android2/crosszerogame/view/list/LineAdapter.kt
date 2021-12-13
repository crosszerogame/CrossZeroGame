package ru.geekbrains.android2.crosszerogame.view.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R

class LineAdapter(
    length: Int,
    private val i: Int,
    private val linear: Linear,
    private val onItemClick: ((Int, Int) -> Unit)
) : RecyclerView.Adapter<CellHolder>() {
    private val cells = mutableListOf<CellValue>()

    init {
        for (i in 0 until length) {
            cells.add(CellValue.EMPTY)
        }
    }

    fun setCrossOn(i: Int) {
        cells[i] = CellValue.CROSS
        notifyItemChanged(i)
    }

    fun setZeroOn(i: Int) {
        cells[i] = CellValue.ZERO
        notifyItemChanged(i)
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
            holder.setItem(i, position, cells[position])
        else
            holder.setItem(position, i, cells[position])

    override fun getItemCount() = cells.size
}