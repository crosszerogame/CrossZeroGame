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
import ru.geekbrains.android2.crosszerogame.R
import ru.geekbrains.android2.crosszerogame.utils.BackEvent
import ru.geekbrains.android2.crosszerogame.utils.getTextColor
import ru.geekbrains.android2.crosszerogame.utils.setSubtitle
import ru.geekbrains.android2.crosszerogame.view.list.FieldAdapter
import ru.geekbrains.android2.crosszerogame.view.list.Linear
import ru.geekbrains.android2.crosszerogame.viewmodel.GameModel
import ru.geekbrains.android2.crosszerogame.viewmodel.GameState

class GameFragment : Fragment(), BackEvent {
    private val model: GameModel by lazy {
        ViewModelProvider(this).get(GameModel::class.java)
    }
    private lateinit var rvField: RecyclerView
    private lateinit var adapter: FieldAdapter
    private var messageBar: Snackbar? = null
    var onMessageAction: (() -> Unit)? = null
    var onHideBottom: (() -> Unit)? = null

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
        model.state.observe(requireActivity()) {
            changeGameState(it)
        }
        model.init()
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
                resizeField(model.fieldSize)
                restoreField()
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
            onHideBottom?.invoke()
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
        for (x in 0 until model.fieldSize) {
            for (y in 0 until model.fieldSize) {
                adapter.setCell(x, y, model.getCell(x, y))
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun changeGameState(state: GameState) {
        dismissMessage()
        setSubtitle("")
        when (state) {
            is GameState.TimeOpponent -> setSubtitle(
                String.format(
                    getString(R.string.time_opponent),
                    state.sec
                )
            )
            is GameState.TimePlayer -> setSubtitle(
                String.format(
                    getString(R.string.time_player),
                    state.sec
                )
            )
            is GameState.PasteChip -> {
                if (adapter.linear == Linear.HORIZONTAL)
                    rvField.smoothScrollToPosition(state.x)
                else
                    rvField.smoothScrollToPosition(state.y)
                doMove(state.x, state.y, state.isCross)
            }
            is GameState.NewGame -> {
                if (state.isWait)
                    setSubtitle(getString(R.string.wait_opponent))
                initField()
            }
            is GameState.NewOpponent ->
                showOpponent(state.nick)
            GameState.WinPlayer -> showMessage(R.string.win_player)
            GameState.WinOpponent -> showMessage(R.string.win_opponent)
            GameState.DrawnGame -> showMessage(R.string.drawn)
            GameState.AbortedGame -> showMessage(R.string.aborted_game)
            GameState.WaitOpponent -> setSubtitle(getString(R.string.wait_opponent))
            GameState.Timeout -> showMessage(R.string.timeout)
        }
    }

    private fun showOpponent(nick: String) {
        if (nick.isEmpty())
            return
        val msg = String.format(
            getString(R.string.connect_opponent),
            nick
        )
        Snackbar.make(rvField, msg, Snackbar.LENGTH_LONG).show()
    }

    private fun showMessage(stringId: Int) {
        messageBar = Snackbar.make(rvField, stringId, Snackbar.LENGTH_INDEFINITE)
        messageBar?.run {
            setActionTextColor(getTextColor())
            setAction(android.R.string.ok) {
                onMessageAction?.invoke()
                dismissMessage()
            }
            show()
        }
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