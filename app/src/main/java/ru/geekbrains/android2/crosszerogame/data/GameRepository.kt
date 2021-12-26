package ru.geekbrains.android2.crosszerogame.data

interface GameRepository {
    fun gamer(
        nikGamer: String = "gamer",
        gameFieldSize: Int = GameConstants.MIN_FIELD_SIZE,
        levelGamer: Int = 1,
        chipImageId: Int = 0,
        timeForTurn: Int = GameConstants.MIN_TIME_FOR_TURN,
    ): Gamer

    fun getOpponent(): Gamer?
    fun setOpponent(key: String, gameStatus: GameConstants.GameStatus = GameConstants.GameStatus.NEW_GAME): Game?
    fun opponentsList(): List<Gamer>
    fun game(
        motionXIndex: Int = -1,
        motionYIndex: Int = -1,
        gameStatus: GameConstants.GameStatus = GameConstants.GameStatus.NEW_GAME
    ): Game

}