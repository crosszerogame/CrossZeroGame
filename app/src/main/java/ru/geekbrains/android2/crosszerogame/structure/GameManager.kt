package ru.geekbrains.android2.crosszerogame.structure

import kotlinx.coroutines.flow.Flow
import ru.geekbrains.android2.crosszerogame.structure.data.Game

interface GameManager {
    val currentGame: Game
    fun createSingleGame(fieldSize: Int, iIsCross: Boolean): GameRepository
    fun createOnlineGame(fieldSize: Int, myNick: String, iIsCross: Boolean, gameLevel: Int): GameRepository
    fun connectTo(game: Game, myNick: String): GameRepository
    fun getGames(): Flow<List<Game>>
    fun getOpponent(): Flow<Opponent>
    fun disconnect()
}