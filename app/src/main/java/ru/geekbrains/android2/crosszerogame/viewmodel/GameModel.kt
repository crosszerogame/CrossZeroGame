package ru.geekbrains.android2.crosszerogame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.geekbrains.android2.crosszerogame.game.GameManagerImpl
import ru.geekbrains.android2.crosszerogame.game.GameRepositoryImpl
import ru.geekbrains.android2.crosszerogame.structure.GameManager
import ru.geekbrains.android2.crosszerogame.structure.data.Cell
import ru.geekbrains.android2.crosszerogame.view.list.CellValue

class GameModel : ViewModel() {
    companion object {
        private const val DELAY_TIME: Long = 100
        private const val DEFAULT_SIZE = 3
        private const val DEFAULT_FIRST = true
        private val parameters: MutableLiveData<GameParameters> = MutableLiveData()

        fun launchGame(value: GameParameters) {
            parameters.value = value
        }
    }

    private val _state: MutableLiveData<GameState> = MutableLiveData()
    val state: LiveData<GameState> = _state
    private val manager: GameManager = GameManagerImpl(GameRepositoryImpl())
    private var size: Int = DEFAULT_SIZE
    val fieldSize: Int
        get() = size
    private var isReady = false

    private val scope = CoroutineScope(
        Dispatchers.Default
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        })

    private fun handleError(error: Throwable) {
        println("Error GameModel:")
        error.printStackTrace()
        _state.postValue(GameState.AbortedGame)
    }

    private val parametersObserver = Observer<GameParameters> {
        when (it) {
            is GameParameters.SingleLaunch ->
                newGame(it.fieldSize, it.beginAsFirst)
            is GameParameters.RemoteLaunch ->
                TODO()
            is GameParameters.RemoteConnect ->
                TODO()
        }
    }

    fun getCell(x: Int, y: Int) = when (manager.getCell(x, y)) {
        Cell.CROSS -> CellValue.CROSS
        Cell.ZERO -> CellValue.ZERO
        else -> CellValue.EMPTY
    }

    fun init() {
        parameters.observeForever(parametersObserver)
        scope.launch {
            manager.state.collect {
                parseGameState(it)
            }
        }
    }

    private suspend fun parseGameState(state: GameManager.State) {
        when (state) {
            is GameManager.State.Move -> parseMove(state)
            GameManager.State.Ready -> isReady = true
            GameManager.State.WaitOpponent -> isReady = false
            GameManager.State.AbortedGame -> this._state.postValue(GameState.AbortedGame)
            GameManager.State.Created -> newGame(DEFAULT_SIZE, DEFAULT_FIRST)
            is GameManager.State.Error -> handleError(state.error)
        }
    }

    private suspend fun parseMove(move: GameManager.State.Move) = move.run {
        if (result != GameManager.Result.CANCEL) {
            postChip(x, y, isCross)
            delay(DELAY_TIME)
        }
        isReady = false
        when (result) {
            GameManager.Result.TURN_PLAYER ->
                isReady = true
            GameManager.Result.WIN_PLAYER ->
                _state.postValue(GameState.WinPlayer)
            GameManager.Result.WIN_OPPONENT ->
                _state.postValue(GameState.WinOpponent)
            GameManager.Result.DRAWN ->
                _state.postValue(GameState.DrawnGame)
            GameManager.Result.TURN_OPPONENT ->
                _state.postValue(GameState.WaitOpponent)
            GameManager.Result.CANCEL ->
                if (manager.gameIsFinish.not()) isReady = true
        }
    }

    override fun onCleared() {
        parameters.removeObserver(parametersObserver)
        scope.cancel()
        super.onCleared()
    }

    private fun newGame(fieldSize: Int, beginAsFirst: Boolean) {
        size = fieldSize
        _state.postValue(GameState.NewGame(fieldSize))
        scope.launch {
            manager.createSingleGame(fieldSize, beginAsFirst)
        }
    }

    fun doMove(x: Int, y: Int) {
        scope.launch {
            if (isReady)
                manager.doMove(x, y)
            else
                parseGameState(manager.lastState)
        }
    }

    private fun postChip(x: Int, y: Int, isCross: Boolean) {
        _state.postValue(GameState.PasteChip(x, y, isCross))
    }
}