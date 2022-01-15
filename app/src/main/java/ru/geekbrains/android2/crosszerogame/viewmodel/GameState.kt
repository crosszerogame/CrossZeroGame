package ru.geekbrains.android2.crosszerogame.viewmodel

sealed class GameState {
    enum class Result {
        CONTINUE, WIN, DRAWN
    }
    data class NewGame(val isWait: Boolean) : GameState()
    data class PasteChip(
        val x: Int,
        val y: Int,
        val isCross: Boolean,
        val result: Result
    ) : GameState()
    data class TimeGamer(val sec: Int) : GameState()
    data class TimeOpponent(val sec: Int) : GameState()
    data class NewOpponent(val nick: String) : GameState()
    object Timeout : GameState()
    object WinGamer : GameState()
    object DrawnGame : GameState()
    object WaitOpponent : GameState()
    object AbortedGame : GameState()
}