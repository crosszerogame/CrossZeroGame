package ru.geekbrains.android2.crosszerogame.xdata

interface GameRepository {
    suspend fun gamer(
        nikGamer: String = "gamer",
        gameFieldSize: Int = GameConstants.MIN_FIELD_SIZE,
        levelGamer: Int = 1,
        chipImageId: Int = 0,
        timeForTurn: Int = GameConstants.MIN_TIME_FOR_TURN,
    ): Gamer

    suspend fun getOpponent(): Gamer?

    suspend fun setOpponent(
        key: String,
        gameStatus: GameConstants.GameStatus = GameConstants.GameStatus.NEW_GAME
    ): Game?

    suspend fun opponentsList(): List<Gamer>

    suspend fun game(
        motionXIndex: Int = -1,
        motionYIndex: Int = -1,
        gameStatus: GameConstants.GameStatus = GameConstants.GameStatus.NEW_GAME
    ): Game

    suspend fun deleteGamer(): Boolean

    fun dotsToWin(gameFieldSize: Int): Pair<Int, Int>
}