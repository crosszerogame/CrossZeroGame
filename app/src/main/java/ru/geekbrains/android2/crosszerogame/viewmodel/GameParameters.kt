package ru.geekbrains.android2.crosszerogame.viewmodel

sealed class GameParameters {
    data class SingleLaunch(
        val fieldSize: Int, val beginAsFirst: Boolean
    ) : GameParameters()

    data class RemoteLaunch(
        val nick: String,
        val waitZero: Boolean,
        val fieldSize: Int,
        val chipsForWin: Int,
        val level: Int,
        val moveTime: Int
    ) : GameParameters()

    data class RemoteConnect(
        val keyOpponent: String,
        val beginAsFirst: Boolean,
        val nikOpponent: String,
        val levelOpponent: Int
    ) : GameParameters()
}