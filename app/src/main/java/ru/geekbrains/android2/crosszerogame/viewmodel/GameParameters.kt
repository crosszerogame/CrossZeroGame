package ru.geekbrains.android2.crosszerogame.viewmodel

import ru.geekbrains.android2.crosszerogame.xdata.GameConstants
import ru.geekbrains.android2.crosszerogame.xdata.Gamer

sealed class GameParameters {
    data class Launch(
        val gamer: Gamer,
        val opponent: Gamer? = null
    ) : GameParameters()

    data class GetOpponent(
        val fieldSize: Int,
        val chipsForWin: Int,
        val nick: String,
        val level: GameConstants.GameLevel
    ) : GameParameters()

    data class SetOpponent(
        val keyOpponent: String,
        val beginAsFirst: Boolean,
        val nikOpponent: String,
        val levelOpponent: GameConstants.GameLevel
    ) : GameParameters()
}