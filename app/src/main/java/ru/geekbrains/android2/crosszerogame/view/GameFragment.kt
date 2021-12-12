package ru.geekbrains.android2.crosszerogame.view

import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.view.list.FieldAdapter
import ru.geekbrains.android2.crosszerogame.view.list.Linear

class GameFragment : Fragment() {
    companion object {
        val parameters: MutableLiveData<GameParameters> = MutableLiveData()
        private const val DEFAULT_SIZE = 3
        private const val DEFAULT_FIRST = true

        fun launchGame(fieldSize: Int, beginAsFirst: Boolean) {
            parameters.value = GameParameters(fieldSize, beginAsFirst)
        }
    }

    data class GameParameters(
        val fieldSize: Int, val beginAsFirst: Boolean
    )

    private lateinit var rvField: RecyclerView
    private lateinit var adapter: FieldAdapter
    private var size: Int = DEFAULT_SIZE
    private var isFirst: Boolean = DEFAULT_FIRST

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvField = view.findViewById(R.id.rv_field) as RecyclerView
        parameters.observe(requireActivity()) {
            rvField.layoutParams = rvField.layoutParams.apply {
                this.width = ViewGroup.LayoutParams.MATCH_PARENT
                this.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            isFirst = it.beginAsFirst
            size = it.fieldSize
            resizeField()
        }
        if (savedInstanceState == null)
            resizeField()
    }

    private fun resizeField() {
        object : CountDownTimer(100, 100) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                initField(size)
            }
        }.start()
    }

    private fun initField(fieldSize: Int) {
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
            fieldSize = fieldSize,
            cellSize = countCellSize(linear, fieldSize),
            linear = linear
        ) { x, y ->
            if (isFirst)
                adapter.setCrossOn(x, y)
            else
                adapter.setZeroOn(x, y)
        }
        rvField.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    private fun countCellSize(linear: Linear, fieldSize: Int): Int {
        var size = if (linear == Linear.HORIZONTAL)
            rvField.height
        else
            rvField.width
        val metrics = resources.displayMetrics
        val cellMargin = (4 * metrics.density).toInt()
        size = size / fieldSize - cellMargin
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