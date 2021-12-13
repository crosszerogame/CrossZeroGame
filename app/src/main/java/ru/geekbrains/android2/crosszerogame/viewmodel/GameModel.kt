package ru.geekbrains.android2.crosszerogame.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import ru.geekbrains.android2.crosszerogame.data.CellField
import ru.geekbrains.android2.crosszerogame.data.Game
import ru.geekbrains.android2.crosszerogame.data.GameStatus
import ru.geekbrains.android2.crosszerogame.data.Gamer
import ru.geekbrains.android2.crosszerogame.data.ai.ArtIntelligence
import ru.geekbrains.android2.crosszerogame.view.list.CellValue

class GameModel : ViewModel() {
    companion object {
        private const val DEFAULT_SIZE = 3
        private const val DEFAULT_FIRST = true
        private val parameters: MutableLiveData<GameParameters> = MutableLiveData()

        fun launchGame(fieldSize: Int, beginAsFirst: Boolean) {
            parameters.value = GameParameters(fieldSize, beginAsFirst)
        }
    }

    private val state: MutableLiveData<GameState> = MutableLiveData()
    private val ai = ArtIntelligence()
    private var gamer = Gamer()
    private var game = Game()
    private var size: Int = DEFAULT_SIZE
    private var isFirst: Boolean = DEFAULT_FIRST

    private val parametersObserver = Observer<GameParameters> {
        size = it.fieldSize
        isFirst = it.beginAsFirst
        newGame()
    }

    fun getState(): LiveData<GameState> = state

    fun getFieldSize() = size

    fun getCell(x: Int, y: Int) = when (game.gameField[y][x]) {
        CellField.GAMER -> if (isFirst) CellValue.CROSS else CellValue.ZERO
        CellField.OPPONENT -> if (isFirst) CellValue.ZERO else CellValue.CROSS
        CellField.EMPTY -> CellValue.EMPTY
    }

    fun init() {
        parameters.observeForever(parametersObserver)
        if (game.gameStatus == GameStatus.NEW_GAME)
            newGame()
        else
            postState()
    }

    override fun onCleared() {
        parameters.removeObserver(parametersObserver)
        super.onCleared()
    }

    private fun newGame() {
        state.value = GameState.NewGame(size)
        gamer = ai.newGamer(size)
        game = ai.game(
            gameStatus = if (isFirst)
                GameStatus.NEW_GAME_FIRST_GAMER
            else
                GameStatus.NEW_GAME_FIRST_OPPONENT
        )
    }

    fun doMove(x: Int, y: Int) {
        if (game.gameStatus == GameStatus.GAME_IS_ON) {
            state.value = GameState.MovePlayer(x, y, isFirst)
            game = ai.game(
                motionXIndex = x,
                motionYIndex = y,
                gameStatus = GameStatus.GAME_IS_ON
            )
        }
        postState()
    }

    private fun postState() {
        when (game.gameStatus) {
            GameStatus.GAME_IS_ON ->
                doAiMove()
            GameStatus.WIN_GAMER ->
                state.value = GameState.WinPlayer
            GameStatus.WIN_OPPONENT -> {
                doAiMove()
                state.value = GameState.WinOpponent
            }
            GameStatus.DRAWN_GAME -> {
                doAiMove()
                state.value = GameState.DrawnGame
            }
            GameStatus.ABORTED_GAME ->
                state.value = GameState.AbortedGame
        }
    }

    private fun doAiMove() {
        if (game.motionXIndex > -1)
            state.value = GameState.MoveOpponent(game.motionXIndex, game.motionYIndex, !isFirst)
    }

    fun readyField() {
        if (game.gameStatus == GameStatus.GAME_IS_ON && !isFirst)
            doAiMove()
    }
}