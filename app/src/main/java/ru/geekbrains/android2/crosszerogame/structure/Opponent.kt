package ru.geekbrains.android2.crosszerogame.structure

import kotlinx.coroutines.flow.Flow
import ru.geekbrains.android2.crosszerogame.structure.state.OpponentState

interface Opponent {
    fun sendMyMove(x: Int, y: Int)
    fun getHisResponse(): Flow<OpponentState>
}