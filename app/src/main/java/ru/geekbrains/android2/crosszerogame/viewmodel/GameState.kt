package ru.geekbrains.android2.crosszerogame.viewmodel

import ru.geekbrains.android2.crosszerogame.xdata.GameConstants

sealed class GameState {
    data class NewGame(
        val remoteOpponent: Boolean,
        val fieldSize: Int,
        val nikOpponent: String = "",
        val levelOpponent: Int = GameConstants.DEFAULT_LEVEL_GAMER,
        val opponentIsFirst: Boolean
    ) : GameState()

    data class MoveGamer(val x: Int, val y: Int, val isCross: Boolean = true) : GameState()
    data class MoveOpponent(val x: Int, val y: Int, val isCross: Boolean = false) : GameState()
    object WinGamer : GameState()
    object WinOpponent : GameState()
    object DrawnGame : GameState()
    object WaitOpponent : GameState()
    object AbortedGame : GameState()
}