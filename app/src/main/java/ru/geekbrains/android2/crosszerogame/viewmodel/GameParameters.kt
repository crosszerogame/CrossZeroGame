package ru.geekbrains.android2.crosszerogame.viewmodel


sealed class GameParameters {
    data class SingleLaunch(
        val fieldSize: Int, val beginAsFirst: Boolean
    ) : GameParameters()

    data class RemoteLaunch(
        val fieldSize: Int, val chipsForWin: Int, val beginAsFirst: Boolean, val nick: String, val level: Int
    ) : GameParameters()

    data class RemoteConnect(
        val nick: String, val idGame: Int
    ) : GameParameters()
}