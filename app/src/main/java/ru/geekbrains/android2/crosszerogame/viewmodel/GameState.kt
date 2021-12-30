package ru.geekbrains.android2.crosszerogame.viewmodel

sealed class GameState {
    data class NewGame(val fieldSize: Int) : GameState()
    data class PasteChip(val x: Int, val y: Int, val isCross: Boolean) : GameState()
    object WinPlayer : GameState()
    object WinOpponent : GameState()
    object DrawnGame : GameState()
    object WaitOpponent : GameState()
    object AbortedGame : GameState()
}