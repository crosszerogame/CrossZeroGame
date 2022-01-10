package ru.geekbrains.android2.crosszerogame.viewmodel

import ru.geekbrains.android2.crosszerogame.data.GameConstants

sealed class GameState {
    data class NewGame(
        val isRemoteOpponent: Boolean,
        val fieldSize: Int,
        val nikOpponent: String = "",
        val levelOpponent: Int = GameConstants.DEFAULT_LEVEL_GAMER,
        val opponentIsFirst: Boolean
    ) : GameState()

    data class PasteChip(val x: Int, val y: Int, val isCross: Boolean) : GameState()
    data class TimePlayer(val sec: Int) : GameState()
    data class TimeOpponent(val sec: Int) : GameState()
    object Timeout : GameState()
    object WinGamer : GameState()
    object WinOpponent : GameState()
    object DrawnGame : GameState()
    object WaitOpponent : GameState()
    object AbortedGame : GameState()
}