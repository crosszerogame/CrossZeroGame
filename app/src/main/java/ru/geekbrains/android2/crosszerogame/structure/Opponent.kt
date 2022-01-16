package ru.geekbrains.android2.crosszerogame.structure

import kotlinx.coroutines.flow.Flow
import ru.geekbrains.android2.crosszerogame.structure.data.MoveResult
import ru.geekbrains.android2.crosszerogame.structure.data.Player

interface Opponent {
    val IsReady: Boolean
    fun preparing(): Flow<Player?>
    fun waitMove(): Flow<Player?>
    suspend fun sendMove(x: Int, y: Int, result: MoveResult)
    suspend fun sendBye()
}