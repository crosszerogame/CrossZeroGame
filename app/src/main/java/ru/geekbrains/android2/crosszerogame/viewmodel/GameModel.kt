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
import ru.geekbrains.android2.crosszerogame.view.list.CellValue

class GameModel : ViewModel() {
    companion object {
        private const val DEFAULT_SIZE = GameConstants.MIN_FIELD_SIZE
        private const val DEFAULT_FIRST = true

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
    private var isFirst: Boolean = DEFAULT_FIRST
    private var remoteOpponent = false

    private val parametersObserver = Observer<GameParameters> {
        when (it) {
            is GameParameters.SingleLaunch -> {
                size = it.fieldSize
                isFirst = it.beginAsFirst
                remoteOpponent = false
                newGameAi()
            }
            is GameParameters.GetOpponent -> {
                size = it.fieldSize
                remoteOpponent = true
                getOpp(it)
            }
            is GameParameters.SetOpponent -> {
                remoteOpponent = true
                setOpp(it)
            }
        }
    }

    private val scope = CoroutineScope(
        Dispatchers.Main
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable -> handleError(throwable) })

    fun getCell(x: Int, y: Int) = when (game.gameField[y][x]) {
        GameConstants.CellField.GAMER -> if (isFirst) CellValue.CROSS else CellValue.ZERO
        GameConstants.CellField.OPPONENT -> if (isFirst) CellValue.ZERO else CellValue.CROSS
        GameConstants.CellField.EMPTY -> CellValue.EMPTY
    }

    fun init() {
        parameters.observeForever(parametersObserver)
        if (!remoteOpponent) {
            if (game.gameStatus == GameConstants.GameStatus.NEW_GAME)
                newGameAi()
            else
                postState()
        }
    }

    override fun onCleared() {
        parameters.removeObserver(parametersObserver)
        super.onCleared()
    }

    private fun newGameAi() {
        _state.value = GameState.NewGame(
            remoteOpponent = false,
            fieldSize = size,
            opponentIsFirst = !isFirst
        )
        scope.launch {
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
    }

    private fun getOpp(params: GameParameters.GetOpponent) {
        scope.launch {
            //регистрируем геймера, если есть, то обновляем
            gamer = gr.gamer(
                gameFieldSize = params.fieldSize,
                nikGamer = params.nick
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
                    remoteOpponent = true,
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
    }

    private fun setOpp(params: GameParameters.SetOpponent) {
        scope.launch {
            gamer.keyOpponent = params.keyOpponent
            var gm: Game? = null
            var attempt = 1
            //почему-то с первой попытки не всегда проходит
            while (gm == null || attempt <= GameConstants.MAX_ATTEMPTS_PUT_DATA_TO_SERVER) {
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
                    remoteOpponent = true,
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
    }

    fun doMove(x: Int, y: Int) {
        if (game.gameStatus == GameConstants.GameStatus.GAME_IS_ON) {
            _state.value = GameState.MoveGamer(x, y, isFirst)

            scope.launch {
                if (remoteOpponent)
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
        if (remoteOpponent) {
            //    TODO()
        } else {
            newGameAi()
        }
    }

    fun abortGame() {
        //   TODO()
    }

    private fun postState() {
        when (game.gameStatus) {
            GameConstants.GameStatus.GAME_IS_ON -> {
                if (game.turnOfGamer) doOpponentMove()
            }
            GameConstants.GameStatus.WIN_GAMER ->
                _state.value = GameState.WinGamer
            GameConstants.GameStatus.WIN_OPPONENT -> {
                doOpponentMove()
                _state.value = GameState.WinOpponent
            }
            GameConstants.GameStatus.DRAWN_GAME -> {
                doOpponentMove()
                _state.value = GameState.DrawnGame
            }
            GameConstants.GameStatus.ABORTED_GAME ->
                _state.value = GameState.AbortedGame
        }
    }

    private fun doOpponentMove() {
        if (game.motionXIndex > -1)
            _state.value = GameState.MoveOpponent(game.motionXIndex, game.motionYIndex, !isFirst)
    }

    fun readyField() {
        if (game.gameStatus == GameConstants.GameStatus.GAME_IS_ON && !isFirst)
            doOpponentMove()
    }

    private fun handleError(error: Throwable) {
        println("Error GameModel:")
        error.printStackTrace()
        _state.postValue(GameState.AbortedGame)
    }
}