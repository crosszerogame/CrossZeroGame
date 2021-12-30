package ru.geekbrains.android2.crosszerogame.game.opponents

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import ru.geekbrains.android2.crosszerogame.structure.Opponent
import ru.geekbrains.android2.crosszerogame.structure.data.Player

class NonOpponent : Opponent {
    override val state: Flow<Opponent.State> = MutableStateFlow(Opponent.State.Leave)

    override fun preparing(): Flow<Player> = flow {
    }

    override suspend fun sendMove(x: Int, y: Int) {
    }

    override suspend fun sendBye() {
    }
}