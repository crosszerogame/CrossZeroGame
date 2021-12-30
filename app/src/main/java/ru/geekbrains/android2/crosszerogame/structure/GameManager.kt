package ru.geekbrains.android2.crosszerogame.structure

import kotlinx.coroutines.flow.Flow
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.structure.data.Cell

interface GameManager {
    enum class Result {
        TURN_PLAYER, TURN_OPPONENT, WIN_PLAYER, WIN_OPPONENT, DRAWN, CANCEL
    }

    sealed class State {
        object Created : State()
        object WaitOpponent : State()
        object Ready : State()
        data class Move(val x: Int, val y: Int, val isCross: Boolean, val result: Result) : State()
        object AbortedGame : State()
        data class Error(val error: Throwable) : State()
    }

    val state: Flow<State>
    val lastState: State
    val gameIsFinish: Boolean
    suspend fun createSingleGame(fieldSize: Int, iIsCross: Boolean)
    suspend fun createRemoteGame(fieldSize: Int, myNick: String, iIsCross: Boolean, gameLevel: Int)
    suspend fun connectTo(game: Game, myNick: String)
    suspend fun doMove(x: Int, y: Int)
    suspend fun finishGame()
    fun getGames(): Flow<List<Game>>
    fun getCell(x: Int, y: Int): Cell
}