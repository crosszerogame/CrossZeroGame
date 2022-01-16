package ru.geekbrains.android2.crosszerogame.game.opponents

import kotlinx.coroutines.flow.*
import ru.geekbrains.android2.crosszerogame.structure.GameClient
import ru.geekbrains.android2.crosszerogame.structure.Opponent
import ru.geekbrains.android2.crosszerogame.structure.data.Game
import ru.geekbrains.android2.crosszerogame.structure.data.MoveResult
import ru.geekbrains.android2.crosszerogame.structure.data.Player

class RemoteOpponent(
    private val game: Game,
    private val iIsCross: Boolean,
    private val client: GameClient
) : Opponent {
    private var isReady = false
    override val IsReady: Boolean
        get() = isReady

    override fun preparing(): Flow<Player?> = flow {
        client.startGame(game, iIsCross).collect {
            isReady = it != null
            emit(it)
        }
    }

    override fun waitMove(): Flow<Player?> =
        client.waitMove()

    override suspend fun sendMove(x: Int, y: Int, result: MoveResult) {
        if (isReady)
            client.postMove(x, y, result)
    }

    override suspend fun sendBye() {
        client.postState(Player.State.LEFT)
    }
}
