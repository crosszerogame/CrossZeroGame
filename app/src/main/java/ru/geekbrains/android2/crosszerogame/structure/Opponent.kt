package ru.geekbrains.android2.crosszerogame.structure

import kotlinx.coroutines.flow.Flow
import ru.geekbrains.android2.crosszerogame.structure.data.Player

interface Opponent {
    sealed class State {
        data class Move(val x: Int, val y: Int) : State()
        object Created : State()
        object Sleep : State()
        object Leave : State()
        data class Error(val error: Throwable) : State()
    }

    val state: Flow<State>
    fun preparing(): Flow<Player>
    suspend fun sendMove(x: Int, y: Int)
    suspend fun sendBye()
}