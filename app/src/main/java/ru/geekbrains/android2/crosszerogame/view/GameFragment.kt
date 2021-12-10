package ru.geekbrains.android2.crosszerogame.view

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.view.list.FieldAdapter
import ru.geekbrains.android2.crosszerogame.view.list.Linear
import android.os.CountDownTimer

class GameFragment : Fragment() {
    private val LENGTH = 5
    private lateinit var rvField: RecyclerView
    private lateinit var adapter: FieldAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvField = view.findViewById(R.id.rv_field) as RecyclerView
        object : CountDownTimer(300, 300) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                initField()
            }
        }.start()
    }

    private fun initField() {
        val linear = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            Linear.HORIZONTAL
        else
            Linear.VERTICAL

        val layoutManager = LinearLayoutManager(
            requireContext(),
            if (linear == Linear.HORIZONTAL)
                LinearLayoutManager.HORIZONTAL
            else
                LinearLayoutManager.VERTICAL,
            false
        )
        rvField.layoutManager = layoutManager

        adapter = FieldAdapter(
            fieldLength = LENGTH,
            cellSize = countCellSize(linear, LENGTH),
            linear = linear
        ) { x, y ->
            adapter.setCrossOn(x, y)
        }
        rvField.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun countCellSize(linear: Linear, length: Int): Int {
        var size = if (linear == Linear.HORIZONTAL)
            rvField.height
        else
            rvField.width
        val metrics = resources.displayMetrics
        val cellMargin = (4 * metrics.density).toInt()
        size = size / length - cellMargin
        rvField.layoutParams = rvField.layoutParams.apply {
            this.width = ViewGroup.LayoutParams.WRAP_CONTENT
            this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        val max = (80 * metrics.density).toInt()
        if (size > max)
            return max
        return size
    }
}