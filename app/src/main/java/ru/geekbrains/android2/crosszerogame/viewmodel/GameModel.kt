package ru.geekbrains.android2.crosszerogame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.geekbrains.android2.crosszerogame.App.Companion.gr
import ru.geekbrains.android2.crosszerogame.App.Companion.grAi
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.GameConstants
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.utils.MoveTimer
import ru.geekbrains.android2.crosszerogame.view.list.CellValue

class GameModel : ViewModel(), MoveTimer.Callback {
    companion object {
        private const val DELAY_TIME: Long = 100
        private const val DEFAULT_SIZE = GameConstants.MIN_FIELD_SIZE
        private const val DEFAULT_FIRST = true
        private const val DEFAULT_SEC_FOR_MOVE = 30
        private val parameters: MutableLiveData<GameParameters> = MutableLiveData()

        fun launchGame(value: GameParameters) {
            parameters.value = value
        }
    }

    private val _state: MutableLiveData<GameState> = MutableLiveData()
    val state: LiveData<GameState> = _state
    private var gamer = Gamer()
    private var opponent = Gamer()
    private var game = Game()
    private var size: Int = DEFAULT_SIZE
    val fieldSize: Int
        get() = size
    private var isReady = false
    private var isTimeout = false
    private var isFirst: Boolean = DEFAULT_FIRST
    private var isRemoteOpponent = false
    private var timer = MoveTimer(this, DEFAULT_SEC_FOR_MOVE)
    private var timeForTurn = DEFAULT_SEC_FOR_MOVE

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
        timer.cancel()
        scope.launch {
            when (it) {
                is GameParameters.SingleLaunch -> {
                    isRemoteOpponent = false
                    doLaunchSingle(it.fieldSize, it.beginAsFirst)
                }
                is GameParameters.RemoteLaunch -> {
                    isRemoteOpponent = true
                    doLaunchRemote(it)
                }
                is GameParameters.RemoteConnect -> {
                    isRemoteOpponent = true
                    doConnect(it)
                }
            }
        }
    }

    fun getCell(x: Int, y: Int): CellValue {
        if (x >= game.gameField.size || y >= game.gameField.size)
            return CellValue.EMPTY
        return when (game.gameField[y][x]) {
            GameConstants.CellField.GAMER -> if (isFirst) CellValue.CROSS else CellValue.ZERO
            GameConstants.CellField.OPPONENT -> if (isFirst) CellValue.ZERO else CellValue.CROSS
            GameConstants.CellField.EMPTY -> CellValue.EMPTY
        }
    }

    fun init() {
        parameters.observeForever(parametersObserver)
        scope.launch {
            if (!isRemoteOpponent) {
                if (game.gameStatus == GameConstants.GameStatus.NEW_GAME)
                    doLaunchSingle(DEFAULT_SIZE, DEFAULT_FIRST)
                else
                    postState(game)
            }
        }
    }

    fun runTimer() {
        if (!isRemoteOpponent) return

        if (timer.secForMove != timeForTurn)
            timer = MoveTimer(this, timeForTurn)

        timer.run()
    }

    override fun onCleared() {
        timer.cancel()
        parameters.removeObserver(parametersObserver)
        scope.cancel()
        super.onCleared()
    }

    private suspend fun doLaunchSingle(fieldSize: Int, beginAsFirst: Boolean) {
        isFirst = beginAsFirst
        size = fieldSize
        _state.postValue(GameState.NewGame(false))
        gamer = grAi.gamer(
            gameFieldSize = fieldSize,
            nikGamer = "Gamer"
        )
        game = grAi.game(
            gameStatus = if (isFirst)
                GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
            else
                GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
        )
        isReady = true
    }

    private suspend fun doLaunchRemote(params: GameParameters.RemoteLaunch) {
        isReady = false
        size = params.fieldSize
        timeForTurn = params.timeForTurn
        isFirst = params.waitZero
        game = Game(
            gameFieldSize = size,
            timeForTurn = timeForTurn
        )
        _state.postValue(GameState.NewGame(true))
        //регистрируем геймера, если есть, то обновляем
        gamer = gr.gamer(
            chipImageId = if (params.waitZero) 0 else 1,
            nikGamer = params.nick,
            gameFieldSize = params.fieldSize,
            levelGamer = params.level,
            timeForTurn = params.timeForTurn
        )
        // очищаем предыдущих оппонентов
        gr.setOpponent(
            key = ""
        )
        //ожидаем появления предложения на игру и принимаем предложение
        gr.flowGetOpponent().collect {
            opponent = it.first
            //game = it.second
            // isFirst = game.turnOfGamer
            isReady = true
            _state.postValue(GameState.NewOpponent(opponent.nikGamer))
            delay(DELAY_TIME)

            gr.flowGame(
                motionXIndex = -1,
                motionYIndex = -1,
                gameStatus = GameConstants.GameStatus.GAME_IS_ON
            ).collect {
                postState(it)
            }
        }
    }

    private suspend fun doConnect(params: GameParameters.RemoteConnect) {
        isReady = false
        _state.postValue(GameState.WaitOpponent)
        gamer.keyOpponent = params.keyOpponent
        var gm: Game? = null
        var attempt = 1
        //почему-то с первой попытки не всегда проходит
        while (gm == null && attempt <= GameConstants.MAX_ATTEMPTS_PUT_DATA_TO_SERVER) {
            //направляем выбранному оппоненту запрос на игру
            gm = gr.setOpponent(
                key = params.keyOpponent,
                gameStatus = when (params.beginAsFirst) {
                    true -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                    false -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                }
            )
            delay(GameConstants.REFRESH_INTERVAL_MS_GET_OPPONENT)
            attempt++
        }

        gm?.let {
            game = it
            size = game.gameFieldSize
            isFirst = game.turnOfGamer
            timeForTurn = game.timeForTurn
            //    val opponent = gr.getOpponent()
            //       opponent?.let {
            _state.postValue(GameState.NewGame(false))
            delay(DELAY_TIME)

            gr.flowGame(
                motionXIndex = -1,
                motionYIndex = -1,
                gameStatus = GameConstants.GameStatus.GAME_IS_ON
            ).collect {
                postState(it)
            }
            //      }
        }
    }

    fun doMove(x: Int, y: Int) {
        if (!isReady || game.gameStatus != GameConstants.GameStatus.GAME_IS_ON)
            return
        _state.postValue(GameState.PasteChip(x, y, isFirst, GameState.Result.CONTINUE))
        isReady = false
        scope.launch {
            val g = if (isRemoteOpponent) gr else {
                delay(DELAY_TIME)
                grAi
            }
            g.flowGame(
                motionXIndex = x,
                motionYIndex = y,
                gameStatus = GameConstants.GameStatus.GAME_IS_ON
            ).collect {
                postState(it)
            }
        }
    }

    fun abortGame() {
        //   TODO()
        _state.postValue(GameState.AbortedGame)
    }

    private fun postState(game: Game) {
        timer.cancel()
        if (game.gameStatus != GameConstants.GameStatus.GAME_IS_ON)
            isReady = false
        when (game.gameStatus) {
            GameConstants.GameStatus.GAME_IS_ON -> {
                if (game.turnOfGamer)
                    doOpponentMove(game.motionXIndex, game.motionYIndex, GameState.Result.CONTINUE)
                else
                    runTimer()
            }
            GameConstants.GameStatus.WIN_GAMER ->
                _state.postValue(GameState.WinGamer)
            GameConstants.GameStatus.WIN_OPPONENT ->
                doOpponentMove(game.motionXIndex, game.motionYIndex, GameState.Result.WIN)
            GameConstants.GameStatus.DRAWN_GAME -> {
                if (game.turnOfGamer)
                    doOpponentMove(game.motionXIndex, game.motionYIndex, GameState.Result.DRAWN)
                else
                    _state.postValue(GameState.DrawnGame)
            }
            GameConstants.GameStatus.ABORTED_GAME ->
                _state.postValue(GameState.AbortedGame)
            else ->
                isReady = true
        }
        this.game = game
    }

    private fun doOpponentMove(x: Int, y: Int, result: GameState.Result) {
        if (x == -1 || y == -1) return
        _state.postValue(GameState.PasteChip(x, y, !isFirst, result))
        isReady = true
    }

    override fun onTime(sec: Int) {
        if (game.turnOfGamer)
            _state.postValue(GameState.TimeGamer(sec))
        else
            _state.postValue(GameState.TimeOpponent(sec))
    }

    override fun onTimeout() {
        isReady = false
        isTimeout = true
        abortGame()
        _state.postValue(GameState.Timeout)
    }
}