package ru.geekbrains.android2.crosszerogame.structure.data

data class Game(
    val id: Int,
    val fieldSize: Int,
    val chipsForWin: Int,
    val level: Int,
    val playerCross: Player?,
    val playerZero: Player?
)
