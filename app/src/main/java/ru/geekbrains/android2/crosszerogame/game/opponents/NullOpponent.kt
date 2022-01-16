package ru.geekbrains.android2.crosszerogame.game.opponents

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.geekbrains.android2.crosszerogame.structure.Opponent
import ru.geekbrains.android2.crosszerogame.structure.data.MoveResult
import ru.geekbrains.android2.crosszerogame.structure.data.Player

class NullOpponent : Opponent {
    private val player = Player(nick = "", state = Player.State.LEFT)
    override val IsReady: Boolean = false

    override fun preparing(): Flow<Player> = flow {
        emit(player)
    }

    override fun waitMove(): Flow<Player> = flow {
        emit(player)
    }

    override suspend fun sendMove(x: Int, y: Int, result: MoveResult) {
    }

    override suspend fun sendBye() {
    }
}