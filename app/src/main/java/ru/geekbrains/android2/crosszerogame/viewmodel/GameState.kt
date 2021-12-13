package ru.geekbrains.android2.crosszerogame.viewmodel

sealed class GameState {
    data class NewGame(val fieldSize: Int): GameState()
    data class MovePlayer(val x: Int, val y: Int, val isCross: Boolean = true): GameState()
    data class MoveOpponent(val x: Int, val y: Int, val isCross: Boolean = false): GameState()
    object WinPlayer: GameState()
    object WinOpponent: GameState()
    object DrawnGame: GameState()
    object WaitOpponent: GameState()
    object AbortedGame : GameState()
}