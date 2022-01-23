package ru.geekbrains.android2.crosszerogame.xdata

interface GameRepository {
    suspend fun gamer(
       gamer: Gamer
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