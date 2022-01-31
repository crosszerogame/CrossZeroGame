package ru.geekbrains.android2.crosszerogame.xdata

data class Gamer(
    var keyGamer: String = "",
    val nikGamer: String = GameConstants.DEFAULT_NICK_GAMER,
    var gameFieldSize: Int = GameConstants.DEFAULT_FIELD_SIZE,
    var levelGamer: GameConstants.GameLevel = GameConstants.DEFAULT_LEVEL_GAMER,
    var chipImageId: Int = 0,
    var timeForTurn: Int = GameConstants.DEFAULT_TIME_FOR_TURN,
    var keyOpponent: String = "",
    var keyGame: String = "",
    var isOnLine:Boolean = false,
    var isFirst:Boolean = GameConstants.DEFAULT_IS_FIRST
) {
    init {
        if (gameFieldSize !in GameConstants.MIN_FIELD_SIZE..GameConstants.MAX_FIELD_SIZE)
            gameFieldSize = GameConstants.MIN_FIELD_SIZE

        if (timeForTurn !in GameConstants.MIN_TIME_FOR_TURN..GameConstants.MAX_TIME_FOR_TURN) timeForTurn =
            GameConstants.MIN_TIME_FOR_TURN
    }
}