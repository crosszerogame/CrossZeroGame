package ru.geekbrains.android2.crosszerogame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import ru.geekbrains.android2.crosszerogame.App.Companion.gr
import ru.geekbrains.android2.crosszerogame.App.Companion.grAi
import ru.geekbrains.android2.crosszerogame.xdata.Game
import ru.geekbrains.android2.crosszerogame.xdata.GameConstants
import ru.geekbrains.android2.crosszerogame.xdata.Gamer

class GameModel : ViewModel() {
    companion object {
        private val parameters: MutableLiveData<GameParameters> = MutableLiveData()
        fun launchGame(value: GameParameters) {
            parameters.value = value
        }
    }

    private val _state: MutableLiveData<GameState> = MutableLiveData()
    val state: LiveData<GameState> = _state
    private var currentGamer = Gamer()
    private var currentOpponent = Gamer()
    private var currentGame = Game()
    val fieldSize: Int
        get() = currentGamer.gameFieldSize

    private val parametersObserver = Observer<GameParameters> {
        when (it) {
            is GameParameters.Launch -> {
                currentGamer = it.gamer
                it.opponent?.let { opp ->
                    currentOpponent = opp
                }
                newGameAi()
            }
            is GameParameters.GetOpponent -> {
                currentGamer.gameFieldSize = it.fieldSize
                currentGamer.isOnLine = true
                getOpp(it, true)
            }
            is GameParameters.SetOpponent -> {
                currentGamer.isOnLine = true
                setOpp(it, true)
            }
        }
    }

    private val scope = CoroutineScope(
        Dispatchers.Main
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable -> handleError(throwable) })

//    fun getCell(x: Int, y: Int) = when (game.gameField[y][x]) {
//        GameConstants.CellField.GAMER -> if (gamerIsFirst) CellValue.CROSS else CellValue.ZERO
//        GameConstants.CellField.OPPONENT -> if (gamerIsFirst) CellValue.ZERO else CellValue.CROSS
//        GameConstants.CellField.EMPTY -> CellValue.EMPTY
//    }

    fun init() {
        parameters.observeForever(parametersObserver)
        if (!currentGamer.isOnLine) {
            if (currentGame.gameStatus == GameConstants.GameStatus.NEW_GAME)
                newGameAi()
        }
    }

    override fun onCleared() {
        parameters.removeObserver(parametersObserver)
        scope.cancel()
        super.onCleared()
    }

    private fun newGameAi() {
        scope.launch {
            currentGamer = grAi.gamer(
                Gamer(
                    gameFieldSize = currentGamer.gameFieldSize,
                    levelGamer = currentGamer.levelGamer
                )
            )
            currentGamer.gameFieldSize = currentGamer.gameFieldSize
            _state.value = GameState.NewGame(
                remoteOpponent = false,
                fieldSize = currentGamer.gameFieldSize,
                opponentIsFirst = !currentGamer.isFirst
            )
        }
    }

    fun readyField() {
        if (!currentGamer.isOnLine)
            scope.launch {
                currentGame = grAi.game(
                    gameStatus = if (currentGamer.isFirst)
                        GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                    else
                        GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                )
                if (!currentGamer.isFirst) postState(
                    currentGamer.isFirst,
                    currentGame.gameStatus,
                    currentGame.motionXIndex,
                    currentGame.motionYIndex,
                    currentGamer.isFirst
                )
            }
    }

    private fun getOpp(params: GameParameters.GetOpponent, resetGamer: Boolean) {
        scope.launch {
            if (resetGamer) {
                //регистрируем геймера, если есть, то обновляем
                currentGamer = gr.gamer(
                    Gamer(
                        gameFieldSize = params.fieldSize,
                        nikGamer = params.nick
                    )
                )
                // очищаем предыдущих оппонентов
                gr.setOpponent(
                    key = ""
                )
            }
            //ожидаем появления предложения на игру и принимаем предложение
            gr.flowGetOpponent().collect { pairOpponent ->
                currentOpponent = pairOpponent.first
                currentGame = pairOpponent.second
                currentGamer.gameFieldSize = currentGame.gameFieldSize
                currentGamer.isFirst = currentGame.turnOfGamer

                _state.value = GameState.NewGame(
                    remoteOpponent = true,
                    fieldSize = currentGamer.gameFieldSize,
                    nikOpponent = currentOpponent.nikGamer,
                    levelOpponent = currentOpponent.levelGamer,
                    opponentIsFirst = !currentGamer.isFirst
                )
                gr.flowGame(
                    motionXIndex = -1,
                    motionYIndex = -1,
                    gameStatus = GameConstants.GameStatus.GAME_IS_ON
                ).collect { pairGame ->
                    currentGame = pairGame.second
                    postState(
                        pairGame.first,
                        pairGame.second.gameStatus,
                        pairGame.second.motionXIndex,
                        pairGame.second.motionYIndex,
                        currentGamer.isFirst
                    )
                }
            }

        }
    }

    private fun setOpp(params: GameParameters.SetOpponent, setNewOpp: Boolean) {
        scope.launch {
            currentGamer.keyOpponent = params.keyOpponent
            var gm: Game? = null
            if (setNewOpp) {
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
            } else

                gm = gr.game(
                    gameStatus = when (params.beginAsFirst) {
                        true -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                        false -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                    }
                )

            gm?.let {
                currentGame = it
                currentGamer.gameFieldSize = currentGame.gameFieldSize
                currentGamer.isFirst = currentGame.turnOfGamer
                val opponent = gr.getOpponent()
                opponent?.let {
                    currentOpponent = opponent
                    _state.value = GameState.NewGame(
                        remoteOpponent = true,
                        fieldSize = currentGamer.gameFieldSize,
                        nikOpponent = opponent.nikGamer,
                        levelOpponent = opponent.levelGamer,
                        opponentIsFirst = !currentGamer.isFirst
                    )
                    gr.flowGame(
                        motionXIndex = -1,
                        motionYIndex = -1,
                        gameStatus = GameConstants.GameStatus.GAME_IS_ON
                    ).collect { pairGame ->
                        currentGame = pairGame.second
                        postState(
                            pairGame.first,
                            pairGame.second.gameStatus,
                            pairGame.second.motionXIndex,
                            pairGame.second.motionYIndex,
                            currentGamer.isFirst
                        )
                    }
                }
            }
        }
    }

    fun doMove(x: Int, y: Int) {
        if (currentGame.gameStatus == GameConstants.GameStatus.GAME_IS_ON) {
            scope.launch {
                if (currentGamer.isOnLine)
                    gr.flowGame(
                        motionXIndex = x,
                        motionYIndex = y,
                        gameStatus = GameConstants.GameStatus.GAME_IS_ON
                    ).collect {
                        currentGame = it.second
                        postState(
                            it.first,
                            it.second.gameStatus,
                            it.second.motionXIndex,
                            it.second.motionYIndex,
                            currentGamer.isFirst
                        )
                    }
                else {
                    grAi.flowGame(
                        motionXIndex = x,
                        motionYIndex = y,
                        gameStatus = GameConstants.GameStatus.GAME_IS_ON
                    ).collect {
                        postState(
                            true,
                            GameConstants.GameStatus.GAME_IS_ON,
                            x,
                            y,
                            currentGamer.isFirst
                        )
                        currentGame = it.second
                        postState(
                            false,
                            it.second.gameStatus,
                            it.second.motionXIndex,
                            it.second.motionYIndex,
                            currentGamer.isFirst
                        )
                    }
                }
            }
        }
    }

    fun repeatGame() {
        if (currentGamer.isOnLine) {
            if (currentGamer.isFirst) {
                val params = GameParameters.SetOpponent(
                    currentOpponent.keyOpponent,
                    false,
                    currentOpponent.nikGamer,
                    currentOpponent.levelGamer
                )
                setOpp(params, false)
            } else {
                val params = GameParameters.GetOpponent(0, 0, "", GameConstants.DEFAULT_LEVEL_GAMER)
                getOpp(params, false)
            }
        } else {
            newGameAi()
        }
    }

    fun abortGame() {
        //   TODO()
    }

    private fun postState(
        moveOfGamer: Boolean,
        gameStatus: GameConstants.GameStatus,
        motionXIndex: Int, motionYIndex: Int,
        isFirst: Boolean
    ) {
        if (moveOfGamer) {
            when (gameStatus) {
                GameConstants.GameStatus.GAME_IS_ON ->
                    _state.value = GameState.MoveGamer(motionXIndex, motionYIndex, isFirst)
                GameConstants.GameStatus.WIN_GAMER -> {
                    _state.value = GameState.MoveGamer(motionXIndex, motionYIndex, isFirst)
                    _state.value = GameState.WinGamer
                }
                GameConstants.GameStatus.DRAWN_GAME -> {
                    //       _state.value = GameState.MoveGamer(motionXIndex, motionYIndex, isFirst)
                    _state.value = GameState.DrawnGame
                }
                GameConstants.GameStatus.ABORTED_GAME -> _state.value = GameState.AbortedGame
            }
        } else {
            when (gameStatus) {
                GameConstants.GameStatus.GAME_IS_ON ->
                    _state.value = GameState.MoveOpponent(motionXIndex, motionYIndex, !isFirst)
                GameConstants.GameStatus.WIN_OPPONENT -> {
                    _state.value =
                        GameState.MoveOpponent(motionXIndex, motionYIndex, !isFirst)
                    _state.value = GameState.WinOpponent
                }
                GameConstants.GameStatus.WIN_GAMER -> {
                    _state.value = GameState.WinGamer
                }
                GameConstants.GameStatus.DRAWN_GAME -> {
                    //      _state.value =
                    //           GameState.MoveOpponent(motionXIndex, motionYIndex, !isFirst)
                    _state.value = GameState.DrawnGame
                }
                GameConstants.GameStatus.ABORTED_GAME -> _state.value = GameState.AbortedGame
            }
        }

    }

    private fun handleError(error: Throwable) {
        println("Error GameModel:")
        error.printStackTrace()
        _state.postValue(GameState.AbortedGame)
    }
}