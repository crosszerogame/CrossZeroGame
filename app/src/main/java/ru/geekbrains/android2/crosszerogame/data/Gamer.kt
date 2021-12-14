package ru.geekbrains.android2.crosszerogame.data

data class Gamer(
    val keyGamer: Int = 1,
    val nikGamer: String = "gamer",
    var gameFieldSize: Int = GameRepository.MIN_FIELD_SIZE,
    var levelGamer: Int = 1,
    var chipImageId: Int = 0,
    var timeForTurn:Int = GameRepository.MIN_TIME_FOR_TURN
) {
    init {
        if (gameFieldSize !in GameRepository.MIN_FIELD_SIZE..GameRepository.MAX_FIELD_SIZE)
            gameFieldSize= GameRepository.MIN_FIELD_SIZE

        if (timeForTurn !in GameRepository.MIN_TIME_FOR_TURN..GameRepository.MAX_TIME_FOR_TURN) timeForTurn =
            GameRepository.MIN_TIME_FOR_TURN
    }
}