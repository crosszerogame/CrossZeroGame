package ru.geekbrains.android2.crosszerogame.game.remote

data class Gamer(
    var keyGamer: String = "",
    val nikGamer: String = "gamer",
    var gameFieldSize: Int = GameConstants.MIN_FIELD_SIZE,
    var levelGamer: Int = 1,
    var chipImageId: Int = 0,
    var timeForTurn: Int = GameConstants.MIN_TIME_FOR_TURN,
    var keyOpponent: String = "",
    var keyGame: String = "",
    var isOnLine: Boolean = false
)