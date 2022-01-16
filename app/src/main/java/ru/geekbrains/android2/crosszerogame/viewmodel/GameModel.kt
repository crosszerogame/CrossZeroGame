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
import ru.geekbrains.android2.crosszerogame.structure.GameRepository
import ru.geekbrains.android2.crosszerogame.structure.data.Cell
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.structure.data.MoveResult
import ru.geekbrains.android2.crosszerogame.structure.data.Player
import ru.geekbrains.android2.crosszerogame.utils.MoveTimer
import ru.geekbrains.android2.crosszerogame.view.list.CellValue

class GameModel : ViewModel(), MoveTimer.Callback {
    companion object {
        private const val DELAY_TIME: Long = 100
        private const val DEFAULT_SIZE = 3
        private const val DEFAULT_FIRST = true
        private const val DEFAULT_SEC_FOR_MOVE = 30
        private val parameters: MutableLiveData<GameParameters> = MutableLiveData()

        fun launchGame(value: GameParameters) {
            parameters.value = value
        }
    }

    private val _state: MutableLiveData<GameState> = MutableLiveData()
    val state: LiveData<GameState> = _state
    private val repository: GameRepository = GameRepositoryImpl()
    private val manager: GameManager = GameManagerImpl(repository)
    private var size: Int = DEFAULT_SIZE
    val fieldSize: Int
        get() = size
    private var isReady = false
    private var isPlayerMove = true
    private var isTimeout = false
    private var isSingleGame = true
    private var isNewGame = true
    private var lastMove = MoveResult.CANCEL
    private var timer = MoveTimer(this, DEFAULT_SEC_FOR_MOVE)
    private var gameJob: Job? = null

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
        finishGame()
    }

    private fun finishGame() {
        timer.cancel()
        isReady = false
        gameJob?.cancel()
        scope.launch {
            manager.finishGame()
        }
    }

    private val parametersObserver = Observer<GameParameters> {
        timer.cancel()
        when (it) {
            is GameParameters.SingleLaunch ->
                newSingleGame(it.fieldSize, it.beginAsFirst)
            is GameParameters.RemoteLaunch ->
                newRemoteGame(it)
            is GameParameters.RemoteConnect ->
                connectRemoteGame(it)
        }
    }

    fun getCell(x: Int, y: Int) = if (isNewGame)
        CellValue.EMPTY
    else {
        when (manager.getCell(x, y)) {
            Cell.CROSS -> CellValue.CROSS
            Cell.ZERO -> CellValue.ZERO
            else -> CellValue.EMPTY
        }
    }

    fun init() {
        parameters.observeForever(parametersObserver)
        scope.launch {
            manager.state.collect {
                if (isTimeout)
                    _state.postValue(GameState.Timeout)
                else
                    parseGameState(it)
            }
        }
    }

    private suspend fun parseGameState(state: GameManager.State) {
        isReady = false
        when (state) {
            is GameManager.State.Move ->
                parseMove(state)
            is GameManager.State.Ready -> {
                delay(DELAY_TIME)
                isNewGame = false
                isReady = true
                _state.postValue(GameState.NewOpponent(state.opponentNick))
                if (!isSingleGame)
                    timer.run()
            }
            GameManager.State.WaitOpponent ->
                _state.postValue(GameState.WaitOpponent)
            GameManager.State.AbortedGame ->
                _state.postValue(GameState.AbortedGame)
            GameManager.State.Created ->
                newSingleGame(DEFAULT_SIZE, DEFAULT_FIRST)
            is GameManager.State.Error ->
                handleError(state.error)
        }
    }

    private suspend fun parseMove(move: GameManager.State.Move) = with(move) {
        if (result != MoveResult.CANCEL) {
            postChip(x, y, isCross)
            delay(DELAY_TIME)
        }
        when (result) {
            MoveResult.MOVE_PLAYER -> {
                isReady = true
                isPlayerMove = true
            }
            MoveResult.WIN_PLAYER ->
                _state.postValue(GameState.WinPlayer)
            MoveResult.WIN_OPPONENT ->
                _state.postValue(GameState.WinOpponent)
            MoveResult.DRAWN ->
                _state.postValue(GameState.DrawnGame)
            MoveResult.MOVE_OPPONENT ->
                isPlayerMove = false
            MoveResult.CANCEL ->
                if (manager.gameIsFinish.not() && isPlayerMove) isReady = true
        }
        if (result != MoveResult.CANCEL)
            runTimer(result)
        lastMove = result
    }

    private fun runTimer(result: MoveResult) {
        if (isSingleGame || lastMove == result)
            return
        if (result == MoveResult.MOVE_PLAYER || result == MoveResult.MOVE_OPPONENT)
            timer.run()
        else
            timer.cancel()
    }

    override fun onCleared() {
        timer.cancel()
        parameters.removeObserver(parametersObserver)
        scope.cancel()
        super.onCleared()
    }

    private fun newSingleGame(fieldSize: Int, beginAsFirst: Boolean) {
        finishGame()
        size = fieldSize
        isTimeout = false
        isSingleGame = true
        isPlayerMove = beginAsFirst
        isNewGame = true
        _state.postValue(GameState.NewGame(false))
        gameJob = scope.launch {
            manager.createSingleGame(fieldSize, beginAsFirst)
        }
    }

    private fun newRemoteGame(parameters: GameParameters.RemoteLaunch) {
        finishGame()
        size = parameters.fieldSize
        isTimeout = false
        isSingleGame = false
        isPlayerMove = parameters.beginAsFirst
        isNewGame = true
        _state.postValue(GameState.NewGame(true))
        timer = MoveTimer(this, parameters.time)
        val player = Player(
            nick = parameters.nick
        )
        gameJob = scope.launch {
            manager.createRemoteGame(
                Game(
                    fieldSize = size,
                    chipsForWin = repository.getChipsForWin(size),
                    level = parameters.level,
                    moveTime = parameters.time,
                    playerCross = if (parameters.beginAsFirst) player else null,
                    playerZero = if (!parameters.beginAsFirst) player else null
                )
            )
        }
    }

    private fun connectRemoteGame(parameters: GameParameters.RemoteConnect) {
        finishGame()
        isTimeout = false
        isSingleGame = false
        val game = parameters.game
        size = game.fieldSize
        isPlayerMove = game.playerCross == null
        isNewGame = true
        _state.postValue(GameState.NewGame(true))
        timer = MoveTimer(this, game.moveTime)
        gameJob = scope.launch {
            manager.connectTo(
                game = game,
                player = Player(nick = parameters.nick)
            )
        }
    }

    fun doMove(x: Int, y: Int) {
        gameJob = scope.launch {
            when {
                isReady -> {
                    isReady = false
                    manager.doMove(x, y)
                }
                isTimeout -> _state.postValue(GameState.Timeout)
                else -> parseGameState(manager.lastState)
            }
        }
    }

    private fun postChip(x: Int, y: Int, isCross: Boolean) {
        _state.postValue(GameState.PasteChip(x, y, isCross))
    }

    override fun onTime(sec: Int) {
        if (isPlayerMove)
            _state.postValue(GameState.TimePlayer(sec))
        else
            _state.postValue(GameState.TimeOpponent(sec))
    }

    override fun onTimeout() {
        isTimeout = true
        finishGame()
        _state.postValue(GameState.Timeout)
    }
}