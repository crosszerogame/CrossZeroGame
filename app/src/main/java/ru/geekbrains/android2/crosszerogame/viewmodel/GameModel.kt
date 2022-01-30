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
                gamerFlowLiveQuery(it.gamer)
                currentGamerGameInit(it.gamer)
                if (it.gamer.isOnLine) {
                    it.opponent?.let { opp ->
                        currentOpponent = opp
                        setOpp(it, true)
                    }
                } else newGameAi()

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
            newGameAi()
        }
    }

    override fun onCleared() {
        parameters.removeObserver(parametersObserver)
        scope.cancel()
        super.onCleared()
    }

    private fun gamerFlowLiveQuery(gamer: Gamer) {
        if (gamer.keyGamer != currentGamer.keyGamer) {
            scope.launch {
                gr.flowGamer(gamer.keyGamer).collect {
                    if (it.keyOpponent != currentGamer.keyOpponent) {
                        currentGamer.keyOpponent = it.keyOpponent
                        currentGamer = gr.gamer(currentGamer)
                    } else {
                        //       currentGamerGameInit(it)
                    }
                }
            }
            scope.launch {
                gr.flowGame(gamer.keyGame).collect { gameOpponent ->
                    gameOpponent.revertGamerToOpponent()
                    if (gameOpponent.gameStatus in arrayOf(
                            GameConstants.GameStatus.NEW_GAME_FIRST_GAMER,
                            GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                        )
                    ) {
                        currentGame = gr.game(gameOpponent)
                        currentGamer.gameFieldSize = currentGame.gameFieldSize
                        currentGamer.isFirst = currentGame.turnOfGamer
                        _state.value = GameState.NewGame(
                            remoteOpponent = true,
                            fieldSize = currentGamer.gameFieldSize,
                            nikOpponent = currentOpponent.nikGamer,
                            levelOpponent = currentOpponent.levelGamer,
                            gamerIsFirst = currentGamer.isFirst
                        )

                    } else {
                        currentGame = gr.game(gameOpponent)
                        if (currentGame.motionXIndex >= 0) postState(
                            !currentGame.turnOfGamer,
                            currentGame.gameStatus,
                            currentGame.motionXIndex,
                            currentGame.motionYIndex,
                            currentGamer.isFirst
                        )
                    }
                }
            }
        }
    }

    private fun currentGamerGameInit(gamer: Gamer) {
        currentGamer = gamer
        currentGame.keyGame = gamer.keyGame
        currentGame.gameFieldSize = gamer.gameFieldSize
        currentGame.turnOfGamer = gamer.isFirst
    }

    private fun newGameAi() {
        scope.launch {
            currentGamer = grAi.gamer(
                currentGamer
            )
            _state.value = GameState.NewGame(
                remoteOpponent = false,
                fieldSize = currentGamer.gameFieldSize,
                gamerIsFirst = currentGamer.isFirst
            )
        }
    }

    fun readyField() {
        if (!currentGamer.isOnLine)
            scope.launch {
                currentGame.gameStatus = if (currentGamer.isFirst)
                    GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                else
                    GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT

                currentGame = grAi.game(currentGame)
                if (!currentGamer.isFirst) postState(
                    currentGamer.isFirst,
                    currentGame.gameStatus,
                    currentGame.motionXIndex,
                    currentGame.motionYIndex,
                    currentGamer.isFirst
                )
            }
    }

    private fun setOpp(params: GameParameters.Launch, setNewOpp: Boolean) {
        scope.launch {
            var gm: Game? = null
            if (setNewOpp) {
                //направляем выбранному оппоненту запрос на игру
                gm = gr.setOpponent(
                    key = params.opponent!!.keyGamer,
                    gameStatus = when (params.gamer.isFirst) {
                        true -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                        false -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                    }
                )

            } else {
                currentGame.gameStatus = when (params.gamer.isFirst) {
                    true -> GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
                    false -> GameConstants.GameStatus.NEW_GAME_FIRST_OPPONENT
                }
                gm = gr.game(currentGame)
            }

            gm?.let {
                currentGame = it
                currentGamer.gameFieldSize = currentGame.gameFieldSize
                currentGamer.isFirst = params.gamer.isFirst
                // val opponent = gr.getOpponent()
                val opponent = params.opponent
                opponent?.let {
                    _state.value = GameState.NewGame(
                        remoteOpponent = true,
                        fieldSize = currentGamer.gameFieldSize,
                        nikOpponent = currentOpponent.nikGamer,
                        levelOpponent = currentOpponent.levelGamer,
                        gamerIsFirst = currentGamer.isFirst
                    )
                }
            }
        }
    }

    fun doMove(x: Int, y: Int) {
        if (currentGame.gameStatus == GameConstants.GameStatus.GAME_IS_ON) {
            scope.launch {
                currentGame.motionXIndex = x
                currentGame.motionYIndex = y
                //      currentGame.turnOfGamer = true
                currentGame.gameStatus = GameConstants.GameStatus.GAME_IS_ON
                if (currentGamer.isOnLine) {
                    if (currentGame.turnOfGamer) {
                        currentGamer = gr.gamer(currentGamer)
                        currentGame = gr.game(currentGame)
                        if (currentGame.motionXIndex >= 0) postState(
                            !currentGame.turnOfGamer,
                            currentGame.gameStatus,
                            currentGame.motionXIndex,
                            currentGame.motionYIndex,
                            currentGamer.isFirst
                        )
                    }
                } else {
                    currentGame = grAi.game(currentGame)
                    if (currentGame.motionXIndex >= 0) {
                        postState(
                            true,
                            GameConstants.GameStatus.GAME_IS_ON,
                            x,
                            y,
                            currentGamer.isFirst
                        )
                        postState(
                            false,
                            currentGame.gameStatus,
                            currentGame.motionXIndex,
                            currentGame.motionYIndex,
                            currentGamer.isFirst
                        )
                    }
                }
            }
        }
    }

    fun repeatGame() {
        if (currentGamer.isOnLine) {
            val params = GameParameters.Launch(
                currentGamer,
                currentOpponent
            )
            setOpp(params, true)
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