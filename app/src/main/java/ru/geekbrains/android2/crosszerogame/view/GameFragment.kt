package ru.geekbrains.android2.crosszerogame.view

import android.content.res.Configuration
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.geekbrains.android2.crosszerogame.App
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.view.list.FieldAdapter
import ru.geekbrains.android2.crosszerogame.view.list.Linear
import ru.geekbrains.android2.crosszerogame.viewmodel.GameModel
import ru.geekbrains.android2.crosszerogame.viewmodel.GameState

class GameFragment : Fragment(), BackEvent {

    private val model: GameModel by lazy {
        ViewModelProvider(this).get(GameModel::class.java).apply {
            App.instance.appComponent.inject(this)
        }
    }

    private lateinit var rvField: RecyclerView
    private lateinit var adapter: FieldAdapter
    private var modelIsInit = false
    private var messageBar: Snackbar? = null
    var onMessageAction: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rvField = view.findViewById(R.id.rv_field) as RecyclerView
        initField()
        model.testRepo()
    }

    private fun initField() {
        rvField.visibility = View.INVISIBLE
        rvField.layoutParams = rvField.layoutParams.apply {
            this.width = ViewGroup.LayoutParams.MATCH_PARENT
            this.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        object : CountDownTimer(500, 50) {
            override fun onTick(millisUntilFinished: Long) {
                if (rvField.width > 0) {
                    onFinish()
                    cancel()
                }
            }

            override fun onFinish() {
                resizeField(model.getFieldSize())
                restoreField()
                model.readyField()
                if (!modelIsInit) {
                    model.getState().observe(requireActivity()) {
                        changeGameState(it)
                    }
                    model.init()
                    modelIsInit = true
                }
                rvField.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun resizeField(fieldSize: Int) {
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
            model.doMove(x, y)
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

    private fun restoreField() {
        val size = model.getFieldSize()
        for (x in 0 until size) {
            for (y in 0 until size) {
                adapter.setCell(x, y, model.getCell(x, y))
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun changeGameState(state: GameState) {
        dismissMessage()
        when (state) {
            is GameState.MoveOpponent -> {
                if (adapter.linear == Linear.HORIZONTAL)
                    rvField.smoothScrollToPosition(state.x)
                else
                    rvField.smoothScrollToPosition(state.y)
                doMove(state.x, state.y, state.isCross)
            }
            is GameState.MovePlayer -> {
                doMove(state.x, state.y, state.isCross)
            }
            is GameState.NewGame -> {
                initField()
            }
            GameState.WinPlayer -> showMessage(R.string.win_player)
            GameState.WinOpponent -> showMessage(R.string.win_opponent)
            GameState.DrawnGame -> showMessage(R.string.drawn)
            GameState.AbortedGame -> showMessage(R.string.aborted_game)
            GameState.WaitOpponent -> showMessage(R.string.wait_opponent, false)
        }
    }

    private fun showMessage(stringId: Int, withAction: Boolean = true) {
        messageBar = Snackbar.make(rvField, stringId, Snackbar.LENGTH_INDEFINITE)
        if (withAction)
            messageBar?.setAction(android.R.string.ok) {
                onMessageAction?.invoke()
                dismissMessage()
            }
        messageBar?.show()
    }

    private fun dismissMessage() {
        messageBar?.let {
            it.dismiss()
            messageBar = null
        }
    }

    private fun doMove(x: Int, y: Int, isCross: Boolean) {
        if (isCross)
            adapter.setCrossOn(x, y)
        else
            adapter.setZeroOn(x, y)
    }

    override fun onBack(): Boolean {
        if (messageBar == null)
            return false
        dismissMessage()
        return true
    }
}