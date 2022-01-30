package ru.geekbrains.android2.crosszerogame.xdata.remote

import ru.geekbrains.android2.crosszerogame.xdata.Game
import ru.geekbrains.android2.crosszerogame.xdata.Gamer
import kotlinx.coroutines.flow.Flow

interface CrossZeroDB {
    suspend fun insGamer(gamer: Gamer): String?
    suspend fun updGamer(key: String, gamer: Gamer): Boolean
    suspend fun delGamer(key: String): Boolean
    suspend fun getGamer(key: String): Gamer?
    suspend fun listGamer(): List<Gamer>?
    suspend fun gamerLiveQuery(key: String): Flow<Gamer>

    suspend fun insGame(game: Game): String?
    suspend fun updGame(key: String, game: Game): Boolean
    suspend fun delGame(key: String): Boolean
    suspend fun getGame(key: String): Game?
    suspend fun listGame(): List<Game>?
    suspend fun gameLiveQuery(key: String): Flow<Game>

}