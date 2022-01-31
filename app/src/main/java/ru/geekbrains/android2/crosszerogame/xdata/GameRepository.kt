package ru.geekbrains.android2.crosszerogame.xdata

interface GameRepository {
    suspend fun gamer(
       gamer: Gamer
    ): Gamer

    suspend fun getOpponent(): Gamer?

    suspend fun setOpponent(
        key: String,
        gameStatus: GameConstants.GameStatus = GameConstants.GameStatus.NEW_GAME_FIRST_GAMER
    ): Game?

    suspend fun opponentsList(): List<Gamer>

    suspend fun game(
        toGame:Game
    ): Game

    suspend fun deleteGamer(): Boolean

    fun dotsToWin(gameFieldSize: Int): Pair<Int, Int>
}