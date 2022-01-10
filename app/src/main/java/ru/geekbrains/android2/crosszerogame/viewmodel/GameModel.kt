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
    private var isPlayerMove = true
    private var isTimeout = false
    private var isFirst: Boolean = DEFAULT_FIRST
    private var isRemoteOpponent = false
    private var timer = MoveTimer(this, DEFAULT_SEC_FOR_MOVE)

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
                    size = it.fieldSize
                    isFirst = it.beginAsFirst
                    isRemoteOpponent = false
                    newGameAi()
                }
                is GameParameters.RemoteLaunch -> {
                    size = it.fieldSize
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

    fun getCell(x: Int, y: Int) = when (game.gameField[y][x]) {
        GameConstants.CellField.GAMER -> if (isFirst) CellValue.CROSS else CellValue.ZERO
        GameConstants.CellField.OPPONENT -> if (isFirst) CellValue.ZERO else CellValue.CROSS
        GameConstants.CellField.EMPTY -> CellValue.EMPTY
    }

    fun init() {
        parameters.observeForever(parametersObserver)
        scope.launch {
            if (!isRemoteOpponent) {
                if (game.gameStatus == GameConstants.GameStatus.NEW_GAME)
                    newGameAi()
                else
                    postState()
            }
        }
    }

    private fun runTimer() {
        if (isRemoteOpponent)
            timer.run()
    }

    override fun onCleared() {
        timer.cancel()
        parameters.removeObserver(parametersObserver)
        scope.cancel()
        super.onCleared()
    }

    private suspend fun newGameAi() {
        _state.postValue(
            GameState.NewGame(
                isRemoteOpponent = false,
                fieldSize = size,
                opponentIsFirst = !isFirst
            )
        )
        gamer = grAi.gamer(
            gameFieldSize = size,
            nikGamer = "Gamer"
        )
        game = grAi.game(
            gameStatus = if (isFirst)
                GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
            else
                GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
        )
    }

    private suspend fun doLaunchRemote(params: GameParameters.RemoteLaunch) {
        _state.postValue(GameState.WaitOpponent)
        //регистрируем геймера, если есть, то обновляем
        gamer = gr.gamer(
            chipImageId = if(params.waitZero) 0 else 1,
            nikGamer = params.nick,
            gameFieldSize = params.fieldSize,
            levelGamer = params.level,
            timeForTurn = params.moveTime
        )
        // очищаем предыдущих оппонентов
        gr.setOpponent(
            key = ""
        )
        //ожидаем появления предложения на игру и принимаем предложение
        gr.flowGetOpponent().collect {
            opponent = it.first
            game = it.second
            size = game.gameFieldSize
            isFirst = game.turnOfGamer
            _state.value = GameState.NewGame(
                isRemoteOpponent = true,
                fieldSize = size,
                nikOpponent = opponent.nikGamer,
                levelOpponent = opponent.levelGamer,
                opponentIsFirst = !isFirst
            )

            gr.flowGame(
                motionXIndex = -1,
                motionYIndex = -1,
                gameStatus = GameConstants.GameStatus.GAME_IS_ON
            ).collect {
                game = it
                postState()
            }
        }
    }

    private suspend fun doConnect(params: GameParameters.RemoteConnect) {
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
            game = gm
            size = game.gameFieldSize
            isFirst = game.turnOfGamer
            //    val opponent = gr.getOpponent()
            //       opponent?.let {
            _state.value = GameState.NewGame(
                isRemoteOpponent = true,
                fieldSize = size,
                nikOpponent = params.nikOpponent,
                levelOpponent = params.levelOpponent,
                opponentIsFirst = !isFirst
            )
            gr.flowGame(
                motionXIndex = -1,
                motionYIndex = -1,
                gameStatus = GameConstants.GameStatus.GAME_IS_ON
            ).collect {
                game = it
                postState()
            }
            //      }
        }
    }

    fun doMove(x: Int, y: Int) {
        if (game.gameStatus == GameConstants.GameStatus.GAME_IS_ON) {
            _state.value = GameState.PasteChip(x, y, isFirst)

            scope.launch {
                if (isRemoteOpponent)
                    gr.flowGame(
                        motionXIndex = x,
                        motionYIndex = y,
                        gameStatus = GameConstants.GameStatus.GAME_IS_ON
                    ).collect {
                        game = it
                        postState()
                    }
                else
                    grAi.flowGame(
                        motionXIndex = x,
                        motionYIndex = y,
                        gameStatus = GameConstants.GameStatus.GAME_IS_ON
                    ).collect {
                        game = it
                        postState()
                    }
            }
        }
    }

    fun repeatGame() {
        scope.launch {
            if (isRemoteOpponent) {
                //    TODO()
            } else {
                newGameAi()
            }
        }
    }

    fun abortGame() {
        //   TODO()
    }

    private suspend fun postState() {
        if (game.gameStatus != GameConstants.GameStatus.ABORTED_GAME) {
            timer.cancel()
            doOpponentMove()
            delay(DELAY_TIME)
        }
        when (game.gameStatus) {
//            GameConstants.GameStatus.GAME_IS_ON ->
//                if (game.turnOfGamer) doOpponentMove()
            GameConstants.GameStatus.WIN_GAMER ->
                _state.postValue(GameState.WinGamer)
            GameConstants.GameStatus.WIN_OPPONENT ->
                _state.postValue(GameState.WinOpponent)
            GameConstants.GameStatus.DRAWN_GAME ->
                _state.postValue(GameState.DrawnGame)
            GameConstants.GameStatus.ABORTED_GAME ->
                _state.postValue(GameState.AbortedGame)
        }
    }

    private fun doOpponentMove() {
        if (game.motionXIndex > -1)
            _state.postValue(GameState.PasteChip(game.motionXIndex, game.motionYIndex, !isFirst))
    }

    override fun onTime(sec: Int) {
        if (isPlayerMove)
            _state.postValue(GameState.TimePlayer(sec))
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