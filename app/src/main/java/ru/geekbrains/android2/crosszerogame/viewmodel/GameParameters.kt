package ru.geekbrains.android2.crosszerogame.viewmodel

sealed class GameParameters {
    data class SingleLaunch(
        val fieldSize: Int, val beginAsFirst: Boolean
    ) : GameParameters()

    data class GetOpponent(
        val fieldSize: Int, val chipsForWin: Int, val nick: String, val level: Int
    ) : GameParameters()

    data class SetOpponent(
        val keyOpponent: String,
        val beginAsFirst: Boolean,
        val nikOpponent: String,
        val levelOpponent: Int
    ) : GameParameters()
}