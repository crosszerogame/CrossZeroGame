package ru.geekbrains.android2.crosszerogame.structure

import kotlinx.coroutines.flow.Flow
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.structure.data.MoveResult
import ru.geekbrains.android2.crosszerogame.structure.data.Player

interface GameClient {
    fun checkNick(nick: String): Flow<Boolean>
    fun startGame(game: Game, iIsCross: Boolean): Flow<Player?>
    fun waitMove(): Flow<Player?>
    suspend fun postMove(x: Int, y: Int, result: MoveResult)
    suspend fun postState(state: Player.State)
    fun loadGamesList(): Flow<List<Game>>
}