package ru.geekbrains.android2.crosszerogame.structure

import kotlinx.coroutines.flow.Flow
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.structure.data.Cell
import ru.geekbrains.android2.crosszerogame.structure.data.MoveResult
import ru.geekbrains.android2.crosszerogame.structure.data.Player

interface GameManager {
    sealed class State {
        object Created : State()
        object WaitOpponent : State()
        data class Ready(val opponentNick: String) : State()
        data class Move(val x: Int, val y: Int, val isCross: Boolean, val result: MoveResult) : State()
        object AbortedGame : State()
        data class Error(val error: Throwable) : State()
    }

    val state: Flow<State>
    val lastState: State
    val gameIsFinish: Boolean
    suspend fun createSingleGame(fieldSize: Int, iIsCross: Boolean)
    suspend fun createRemoteGame(game: Game)
    suspend fun connectTo(game: Game, player: Player)
    suspend fun doMove(x: Int, y: Int)
    suspend fun finishGame()
    fun getGames(): Flow<List<Game>>
    fun getCell(x: Int, y: Int): Cell
}