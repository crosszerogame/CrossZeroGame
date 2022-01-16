package ru.geekbrains.android2.crosszerogame.viewmodel

import ru.geekbrains.android2.crosszerogame.structure.data.Game

sealed class GameParameters {
    data class SingleLaunch(
        val fieldSize: Int, val beginAsFirst: Boolean
    ) : GameParameters()

    data class RemoteLaunch(
        val fieldSize: Int,
        val chipsForWin: Int,
        val beginAsFirst: Boolean,
        val nick: String,
        val level: Int,
        val time: Int
    ) : GameParameters()

    data class RemoteConnect(
        val nick: String, val game: Game
    ) : GameParameters()
}