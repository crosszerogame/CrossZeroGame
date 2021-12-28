package ru.geekbrains.android2.crosszerogame.structure.state

sealed class OpponentState {
    data class Move(val x: Int, val y: Int): OpponentState()
    object Leave: OpponentState()
    data class Error(val error: Throwable): OpponentState()
}
